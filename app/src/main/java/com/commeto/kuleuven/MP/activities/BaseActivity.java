package com.commeto.kuleuven.MP.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.commeto.kuleuven.MP.adapters.BasePagerAdapter;
import com.commeto.kuleuven.MP.dataClasses.HTTPResponse;
import com.commeto.kuleuven.MP.dialogs.BaseConfirmDialogBuilder;
import com.commeto.kuleuven.MP.dialogs.BaseEditDialogBuilder;
import com.commeto.kuleuven.MP.interfaces.AsyncResponseInterface;
import com.commeto.kuleuven.MP.interfaces.RouteListInterface;
import com.commeto.kuleuven.MP.interfaces.BaseDialogInterface;
import com.commeto.kuleuven.MP.interfaces.SyncInterface;
import com.commeto.kuleuven.MP.listeners.MenuIconUnderlineListener;
import com.commeto.kuleuven.MP.R;
import com.commeto.kuleuven.MP.sqlSupport.LocalDatabase;
import com.commeto.kuleuven.MP.sqlSupport.LocalRoute;
import com.commeto.kuleuven.MP.services.SyncService;
import com.commeto.kuleuven.MP.services.SyncServiceConnection;
import com.commeto.kuleuven.MP.support.ExternalIO;
import com.commeto.kuleuven.MP.support.InternalIO;
import com.commeto.kuleuven.MP.views.LockableViewPager;

import java.util.List;

import static com.commeto.kuleuven.MP.http.HTTPStatic.convertInputStreamToString;
import static com.commeto.kuleuven.MP.support.NotifyStatic.postNotification;
import static com.commeto.kuleuven.MP.support.Static.isNetworkAvailable;
import static com.commeto.kuleuven.MP.support.Static.makeToastLong;
import static com.commeto.kuleuven.MP.support.Static.scaleMenuIcon;
import static com.commeto.kuleuven.MP.support.Static.tryLogin;

/**
 * <pre>
 * Created by Jonas on 1/03/2018.
 *
 * Core activity of the application. This activity displays what is essentially the home screen.
 * </pre>
 *
 * <p>
 * Entry from:
 * </p>
 * <ul>
 *     <li>
 *  LoginActivity
 *      </li>
 *  </ul>
 *
 * <p>
 * Next activities:
 * </p>
 * <ul>
 *  <li>
 *  MeasuringActivity
 *  </li>
 *  <li>
 *  CalibrationActivity
 *  </li>
 *  <li>
 *  LoginActivity
 *  </li>
 *  <li>
 *  FilterSortActivity
 *  </li>
 * </ul>
 *
 * <p>
 * Finishes going to:
 * </p>
 * <ul>
 *  <li>
 *  LoginActivity
 *  </li>
 *  <li>
 *  onBackPressed
 *  </li>
 *  <li>
 *  401 http response
 *  </li>
 * </ul>
 */

public class BaseActivity extends AppCompatActivity{
//==================================================================================================
    //constants for shared preferences

    //Not declared final due to the fact that a context is needed to fetch the constants.
    private String CALIBRATION;
    private String IP;
    private String SOCKET;
    private String IP_DEFAULT;
    private String SOCKET_DEFAULT;

//==================================================================================================
    //interfaces

    /**
     * <p>
     * Interface used to send a request to /MP/service/secured/tokenvalid to see if device is still
     * authorized for the current user.
     * </p>
     *
     * <p>
     * responses:
     * </p>
     * <pre>
     *  401:    User no longer authorized.
     *              - go to LoginActivity.
     *              - Clear SharedPreferences.
     *  -2:     Means no connection could be established.
     *              - continue normal behavior, give notification.
     * </pre>
     */

    private AsyncResponseInterface loginSucces = new AsyncResponseInterface() {
        @Override
        public void processFinished(HTTPResponse response) {
            if(response != null && response.getResponseCode() == 401){
                startActivity(new Intent(context, LoginActivity.class));
                try {
                    preferences.edit().clear().apply();
                    finish();
                } catch (NullPointerException e){
                    InternalIO.writeToLog(context, e);
                }
            } else if(response != null && response.getResponseCode() == -2){
                postNotification(context, getString(R.string.offline_mode), getString(R.string.no_internet_message));
            }
        }
    };

    /**
     * <pre>
     * Interface to call when syncing is complete.
     *
     *  - Resets list of routes.
     *  - stops the SyncService.
     *  - Re-enables the button that starts the SyncService.
     *  </pre>
     */

    private SyncInterface syncInterface = new SyncInterface() {
        @Override
        public void endSync() {
            routeListInterface.resetList(null);
            stopService(syncIntent);
            findViewById(R.id.sync_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sync(view);
                }
            });
            basePagerAdapter.updateStatsLayout();
        }
    };

//==================================================================================================
    //class atributes

    private Long lastClicked;

    private Context context;
    private LockableViewPager basePager;
    private BasePagerAdapter basePagerAdapter;
    private RouteListInterface routeListInterface;

    private String toWrite;
    private DialogInterface dialog;

    private Drawable resetDrawable;
    private ImageView currentImageView;
    private TextView currentText;

    private MenuIconUnderlineListener startListener;
    private MenuIconUnderlineListener profileListener;
    private MenuIconUnderlineListener listListener;
    private MenuIconUnderlineListener mapListener;
    private MenuIconUnderlineListener settingsListener;
    private MenuIconUnderlineListener resetListener;

    private SyncServiceConnection syncServiceConnection = new SyncServiceConnection();
    private Intent syncIntent;

    private SharedPreferences preferences;

//==================================================================================================
    //lifecycle methods

    @Override
    public void onCreate(Bundle bundle){

        //Needed to initiate the activity
        super.onCreate(bundle);
        setContentView(R.layout.activity_base);

        //Getting the used constants
        CALIBRATION = getString(R.string.preferences_calibration);
        IP = getString(R.string.preferences_ip);
        SOCKET = getString(R.string.preferences_socket);
        IP_DEFAULT = getString(R.string.hard_coded_ip);
        SOCKET_DEFAULT = getString(R.string.hard_coded_socket);

        //Further attribute declaration.
        this.context = getApplicationContext();
        toWrite = "";
        preferences  = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        lastClicked = null;
        syncServiceConnection.setSyncInterface(syncInterface);
        syncIntent = new Intent(this, SyncService.class);
        BaseActivity.this.bindService(syncIntent, syncServiceConnection, BIND_AUTO_CREATE);
        resetDrawable = null;
        currentImageView = null;
        currentText = null;

        startListener = new MenuIconUnderlineListener(
                context,
                (ImageView) findViewById(R.id.start_icon),
                R.drawable.logo_light, R.drawable.logo_svg_clicked
        );
        profileListener =new MenuIconUnderlineListener(
                context,
                (ImageView) findViewById(R.id.profile_icon),
                R.drawable.ic_user, R.drawable.ic_user_clicked
        );
        listListener = new MenuIconUnderlineListener(
                context,
                (ImageView) findViewById(R.id.list_icon),
                R.drawable.ic_list, R.drawable.ic_list_clicked
        );
        mapListener = new MenuIconUnderlineListener(
                context,
                (ImageView) findViewById(R.id.map_icon),
                R.drawable.ic_map, R.drawable.ic_map_clicked
        );
        settingsListener = new MenuIconUnderlineListener(
                context,
                (ImageView) findViewById(R.id.settings_icon),
                R.drawable.ic_settings, R.drawable.ic_settings_clicked
        );

        findViewById(R.id.to_start).setOnTouchListener(startListener);
        findViewById(R.id.to_profile).setOnTouchListener(profileListener);
        findViewById(R.id.to_list).setOnTouchListener(listListener);
        findViewById(R.id.to_map).setOnTouchListener(mapListener);
        findViewById(R.id.to_settings).setOnTouchListener(settingsListener);
        resetListener = startListener;

        //Setting size of menu tab icons, equal parts of total screen width divided by 2.
        int value = (scaleMenuIcon() / 2);

        //Icon for going to HomeScreenfragment bigger.
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                value + 24, value + 24
        );
        params.setMargins(12, 12, 12, 12);
        findViewById(R.id.start_icon).setLayoutParams(params);

        params = new LinearLayout.LayoutParams(
                value - 24, value - 24
        );
        params.setMargins(12, 12, 12, 12);
        findViewById(R.id.profile_icon).setLayoutParams(params);
        findViewById(R.id.list_icon).setLayoutParams(params);
        findViewById(R.id.map_icon).setLayoutParams(params);
        findViewById(R.id.settings_icon).setLayoutParams(params);

        //Adding BasePagerAdapter and setting ViewPager to hold fragments
        basePager = findViewById(R.id.base_pager);
        basePagerAdapter = new BasePagerAdapter(getSupportFragmentManager());
        routeListInterface = basePagerAdapter.getRouteListInterface();
        basePager.setAdapter(basePagerAdapter);
        basePager.setOffscreenPageLimit(4);

        //First fragment to be shown, calling function sets tab icon as well as fragment.
        toStart(null);

        //Check if already calibrate.
        try {
            if (preferences.getInt("calibration", 0) == 0) {
                Intent intent = new Intent(this, Callibration.class);
                startActivityForResult(intent, 0);
            }
        } catch (Exception e){

            if (preferences.getFloat("calibration", 0) == 0) {
                Intent intent = new Intent(this, Callibration.class);
                startActivityForResult(intent, 0);
            }
        }

        if(preferences.getBoolean("auto_sync_option", true)) sync(null);
    }

    @Override
    public void onStart(){

        //check if token still valid.
        tryLogin(context, loginSucces);

        super.onStart();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        //unbind syncService onDestroy and dismiss dialog to avoid leaks!
        BaseActivity.this.unbindService(syncServiceConnection);
        if(dialog != null) dialog.dismiss();
    }
//==================================================================================================
    //button actions

    //menu tabs ------------------------------------------------------------------------------------

    /**
     * Switch to HomescreenFragment and style menu icon as clicked.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void toStart(View view){
        basePager.setCurrentItem(2);
        switchActive((ImageView) findViewById(R.id.start_icon), null, R.drawable.logo_svg_clicked);
        startListener.setEnable(false);
        resetListener = startListener;
    }

    /**
     * Switch to ProfileFragment and style menu icon as clicked.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void toProfile(View view){
        basePager.setCurrentItem(1);
        switchActive((ImageView) findViewById(R.id.profile_icon), (TextView) findViewById(R.id.user_text), R.drawable.ic_user_clicked);
        profileListener.setEnable(false);
        resetListener = profileListener;
    }

    /**
     * Switch to RouteListFragment and style menu icon as clicked.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void toList(View view){
        basePager.setCurrentItem(3);
        switchActive((ImageView) findViewById(R.id.list_icon), (TextView) findViewById(R.id.list_text), R.drawable.ic_list_clicked);
        listListener.setEnable(false);
        resetListener = listListener;
    }

    /**
     * Switch to GlobalMapFragment and style menu icon as clicked.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void toMap(View view){
        basePager.setCurrentItem(0);
        switchActive((ImageView) findViewById(R.id.map_icon), (TextView) findViewById(R.id.map_text), R.drawable.ic_map_clicked);
        mapListener.setEnable(false);
        resetListener = mapListener;
    }

    /**
     * Switch to SettingsFragment and style menu icon as clicked.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void toSettings(View view){
        basePager.setCurrentItem(4);
        switchActive((ImageView) findViewById(R.id.settings_icon), (TextView) findViewById(R.id.settings_text), R.drawable.ic_settings_clicked);
        settingsListener.setEnable(false);
        resetListener = settingsListener;
    }

    //homescreen fragment --------------------------------------------------------------------------

    /**
     * <pre>
     * Button to go to MeasuringActivity.
     *
     * Different actions required in coming activities and services depending on choice made.
     * Choices gotten from array in resources.
     *
     * measuring only allowed for devices with an acceleration sensor that can go over 4g (4 times
     *  gravity. "Meten" option not allowed for devices who can't reach this threshold.
     *
     * Choices:
     *  - 0 - ofroad:
     *      . No vibration measurement needed.
     *      . Differnt from sport since coords won't be snapped server side.
     *  - 1 - sport:
     *      . No vibration measurement needed.
     *  - 2 - plezier:
     *      . Vibration values measured and stored when the speed is correct
     *      . No feedback when speed too fast/slow.
     *  - 3 - meten:
     *      . Vibration values measured and stored when the speed is correct
     *      . feedback given when speed too fast/slow.
     * </pre>
     *
     * onClick used in HomeScreenFragment.
     *
     * @param view The clicked view.
     */

    public void start(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(context.getResources().getString(R.string.option_title));
        builder.setItems(R.array.options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                boolean allowed;
                try {allowed = preferences.getInt(CALIBRATION, 0) > 45;
                } catch (Exception e){
                    allowed = preferences.getFloat(CALIBRATION, 0) > 45;
                }

                if(!allowed && i == 3){
                    makeToastLong(context, "niet toegestaan met uw apparaat.");
                } else {
                    Intent intent = new Intent(context, MeasuringActivity.class);

                    intent.putExtra("offroad", i == 0);
                    intent.putExtra("keep", (i == 2 || i == 3) && allowed);
                    intent.putExtra("measuring", i == 3);

                    String[] options = context.getResources().getStringArray(R.array.options);
                    intent.putExtra("type", options[i]);

                    startActivityForResult(intent, 1);
                }

                if(dialogInterface != null) dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    //settings fragment ----------------------------------------------------------------------------

    /**
     * Clear shared preferences, open new CheckLoginActivity and finish activity. Button will open
     * dialog to confirm. onClick used in SettingsFragment.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void logout(View view){


        BaseDialogInterface baseDialogInterface = new BaseDialogInterface() {
            @Override
            public void confirm(String string) {

                preferences.edit().clear().apply();
                //Go to CheckLoginActivity to reset certificate if needed.
                //CheckLogin wil reroute to LoginActivity.
                Intent intent = new Intent(context, CheckLoginActivity.class);
                startActivity(intent);
                finish();
            }
        };

        new BaseConfirmDialogBuilder(
                this,
                getString(R.string.logout) + "?",
                baseDialogInterface
        ).show();
    }

    /**
     * Method should be unnecessary, but can be used to resend a message to the server to get a
     * new token. Method will finish activity and open a new CheckLoginActivity. Opens dialog to
     * confirm. onClick used in SettingsFragment.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void relogin(View view){

        BaseDialogInterface baseDialogInterface = new BaseDialogInterface() {
            @Override
            public void confirm(String string) {

                //Go to CheckLoginActivity to reset certificate if needed.
                Intent intent = new Intent(context, CheckLoginActivity.class);
                startActivity(intent);
                finish();
            }
        };
        new BaseConfirmDialogBuilder(
                this,
                getString(R.string.relogin) + "?",
                baseDialogInterface
        ).show();
    }

    /**
     * Method used to recalibrate the device. onClick used in SettingsFragment.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void recalibrate(View view){
        Intent intent = new Intent(this, Callibration.class);
        startActivityForResult(intent, 0);
    }

    /**
     * Used to reset the IP address of the server to the hardcoded address. Opens a dialog
     * confirm the action. onClick used in SettingsFragment.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void resetIp(View view){

        BaseDialogInterface dialogInterface = new BaseDialogInterface() {
            @Override
            public void confirm(String string) {

                preferences.edit().putString(IP, IP_DEFAULT).apply();
                ((TextView) findViewById(R.id.ip)).setText(getString(R.string.hard_coded_ip));
            }
        };

        BaseConfirmDialogBuilder confirmDialog = new BaseConfirmDialogBuilder(
                this,
                "Huidig: " + preferences.getString(IP, IP_DEFAULT) + ", vervangen?",
                dialogInterface
        );

        confirmDialog.show();
    }

    /**
     * Method used to set the ip address to a new ip address. Opens a new dialog containing an
     * EditText. onClick used in SettingsFragment.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void setIp(View view){

        BaseDialogInterface dialogInterface = new BaseDialogInterface() {
            @Override
            public void confirm(String string) {
                if(!string.equals("")) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(IP, string);
                    editor.apply();
                }
                ((TextView) findViewById(R.id.ip)).setText(string);
            }
        };

        String content = preferences.getString(IP, IP_DEFAULT);
        BaseEditDialogBuilder editDialog = new BaseEditDialogBuilder(
                this,
                "Huidig:\n" + content,
                content,
                dialogInterface
        );

        editDialog.show();
    }

    /**
     * Used to reset the socket used at the server to the hardcoded socket. Opens a dialog
     * confirm the action. onClick used in SettingsFragment.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void resetSocket(View view){

        BaseDialogInterface dialogInterface = new BaseDialogInterface() {
            @Override
            public void confirm(String string) {

                preferences.edit().putString(SOCKET, SOCKET_DEFAULT).apply();
                ((TextView) findViewById(R.id.socket)).setText(SOCKET_DEFAULT);
            }
        };

        BaseConfirmDialogBuilder confirmDialog = new BaseConfirmDialogBuilder(
                this,
                "Huidig:" + preferences.getString(SOCKET, SOCKET_DEFAULT) + ", vervangen?",
                dialogInterface
        );

        confirmDialog.show();
    }

    /**
     * Method used to set the socket to a new socket. Opens a new dialog containing an
     * EditText. onClick used in SettingsFragment.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void setSocket(View view){

        BaseDialogInterface dialogInterface = new BaseDialogInterface() {
            @Override
            public void confirm(String string) {
                if(!string.equals("")) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(SOCKET, string);
                    editor.apply();
                }
                ((TextView) findViewById(R.id.socket)).setText(string);
            }
        };

        String content = preferences.getString("socket", SOCKET_DEFAULT);
        BaseEditDialogBuilder editDialog = new BaseEditDialogBuilder(
                this,
                "Huidig:\n" + content,
                content,
                dialogInterface
        );

        editDialog.show();
    }

    /**
     * Method used to export the log file. Uses the ExternalIO class and uses onActivityResult of
     * the current activity to pass the data. onClick used in SettingsFragment.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void exportLog(View view){
        try {
            toWrite = convertInputStreamToString(InternalIO.getInputStream(context, "log"));
        } catch (Exception e){
            toWrite = "probleem bij log exporteren";
        }
        ExternalIO.createFile(this, "text/plain", "log.txt");
    }

    /**
     * Method used to delete the log file. onClick used in SettingsFragment.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void deleteLog(View view){
        InternalIO.writeToLog(context, "");
    }

    /**
     * Method used to toggle The auto_upload_option Switch when clicking the wrapping LinearLayout.
     * onClick used in SettingsFragment.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void autoUploadSetting(View view){
        ((Switch) findViewById(R.id.auto_upload_option)).toggle();
    }

    /**
     * Method to toggle The auto_sync_option Switch when clicking the wrapping LinearLayout.
     * onClick used in SettingsFragment.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void autoSyncSetting(View view){
        ((Switch) findViewById(R.id.auto_sync_option)).toggle();
    }

    /**
     * Method used to toggle The export_full_switch Switch when clicking the wrapping LinearLayout.
     * onClick used in SettingsFragment.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void exportFullSetting(View view){

        ((Switch) view.findViewById(R.id.export_full_switch)).toggle();
    }

    /**
     * Method used to toggle The debug_switch Switch when clicking the wrapping LinearLayout.
     * onClick used in SettingsFragment.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void debugSetting(View view){

        ((Switch) view.findViewById(R.id.debug_switch)).toggle();
    }

    /**
     * Debug method used to set all current rides in the Room database as "not sent". Opens a
     * dialog to confirm action. onClick used in SettingsFragment.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void setAllNotSent(View view){

        BaseDialogInterface baseDialogInterface = new BaseDialogInterface() {
            @Override
            public void confirm(String string) {
                LocalDatabase localDatabase;
                List<LocalRoute> routes = (localDatabase = LocalDatabase.getInstance(context)).localRouteDAO().debug();
                for(LocalRoute route: routes){
                    route.setSent(false);
                    localDatabase.localRouteDAO().update(route);
                }
            }
        };

        BaseConfirmDialogBuilder builder = new BaseConfirmDialogBuilder(
                this,
                getString(R.string.warning),
                baseDialogInterface
        );
        builder.setMessage(getString(R.string.set_all_not_sent_message));
        builder.show();
    }

    //globalmap fragment ---------------------------------------------------------------------------

    /**
     * Method to hide the searchbar in the GlobalMapFragment when it is no longer needed.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void endSearch(View view){
        findViewById(R.id.search_bar_container).setVisibility(View.GONE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search(view);
            }
        });
    }

    /**
     * Method used to show the searchbar in the GlobalMapFragment.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void search(View view){
        findViewById(R.id.search_bar_container).setVisibility(View.VISIBLE);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endSearch(view);
            }
        });
    }

    /**
     * Used to show and hide the map's legend in the GlobalMapFragment.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void info(View view){
        findViewById(R.id.legend).setVisibility(
                findViewById(R.id.legend).getVisibility() == View.VISIBLE ?
                View.GONE : View.VISIBLE
        );
        findViewById(R.id.info_button).setVisibility(
                findViewById(R.id.info_button).getVisibility() == View.VISIBLE ?
                View.GONE : View.VISIBLE
        );
    }

    /**
     * Used to display the ListView to choose which map to show in the GlobalMapFragment.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void mapOption(View view){

        int visibility = findViewById(R.id.map_option_list).getVisibility();
        findViewById(R.id.map_option_list).setVisibility(visibility == View.VISIBLE ?
                View.GONE : View.VISIBLE
        );
    }

    // ridelist fragment ---------------------------------------------------------------------------

    /**
     * Used to open a new FilterSortActivity in order to edit the filter for the RouteListFragment.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void sort(View view){
        startActivityForResult(
                new Intent(this, FilterSortActivity.class).putExtra("options", routeListInterface.getPrevious()),
                581
        );
    }

    /**
     * Used to show the searchbar in the RouteListFragment. Sets onClickListener for this view
     * to hideSearchList(View view).
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void searchList(View view){
        findViewById(R.id.list_search_bar_container).setVisibility(View.VISIBLE);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSearchList(view);
            }
        });
    }

    /**
     * Used to hide the searchbar in the RouteListFragment. Doing so will automatically reset the
     * list. Sets onClickListener for this view to earchList(View view).
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void hideSearchList(View view){
        findViewById(R.id.list_search_bar_container).setVisibility(View.GONE);
        routeListInterface.resetList();
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchList(view);
            }
        });
    }

    /**
     * Used to start syncing. When sync started sets onClickListener to disableSync(View view).
     * Used in onClick in RouteListFragment.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void sync(View view){
        if(isNetworkAvailable(context)) {
            startService(syncIntent);
        }
        if(view != null) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    disableSync(view);
                }
            });
        }
    }

    /**
     * Used to block the sync button while the SyncService is still running to avoid duplicates.
     * Used in onClick in RouteListFragment.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void disableSync(View view){
        makeToastLong(context, getString(R.string.already_syncing));
    }
//==================================================================================================
    //private methods

    /**
     * <pre>
     * method to easily switch the tab of the fragment currently displayed.
     *
     *  - Reset current active tab.
     *  - set new current attributes.
     *  - Set new current's styles
     *  - Enable currently disabled onTouchListener.
     * </pre>
     *
     * @param newCurrentImageView ImageView of the currently active tab.
     * @param newCurrentText textview of the currently active tab.
     * @param iconId id of drawable for active tab's ImageView.
     */

    private void switchActive(ImageView newCurrentImageView, TextView newCurrentText, int iconId){

        if(currentImageView != null) currentImageView.setImageDrawable(resetDrawable);
        if(currentText != null) currentText.setTextColor(getResources().getColor(R.color.logo_text));

        resetDrawable = newCurrentImageView.getDrawable();
        currentImageView = newCurrentImageView;
        currentText = newCurrentText;

        currentImageView.setImageDrawable(getResources().getDrawable(iconId));
        if(currentText != null) currentText.setTextColor(getResources().getColor(R.color.accent));

        resetListener.setEnable(true);
    }
//==================================================================================================
    //activity result

    /**
     * <pre>
     * - Resets ride list as default if the requestCode wasn't 581.
     *
     * Different request codes:
     *  - 0: used for starting CalibrationActivity.
     *  - 43: android system request code, code to create file in external storage.
     *  - 581: used to identify FilterSortActivity, resets list to the result of this activity.
     * </pre>
     *
     * @param requestCode Request code from startActivityForResult call.
     * @param resultCode Result code from startActivityForResult call.
     * @param data Result data from startActivityForResult call.
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){

        if(resultCode != 581) routeListInterface.resetList(null);

        if(requestCode == 0){
            boolean test = preferences.getInt(CALIBRATION, 0) > 45;
            try {
                findViewById(R.id.can_measure).setVisibility(test ? View.VISIBLE : View.GONE);
                findViewById(R.id.can_not_measure).setVisibility(test ? View.GONE : View.VISIBLE);
            } catch (NullPointerException e){
                //catch NullPointerException thrown when view not fully initiated when logging in.
            }
        }
        if(requestCode == 43) ExternalIO.alterDocument(this, toWrite, requestCode, requestCode, data);
        if(requestCode == 581 && resultCode == RESULT_OK){
            Bundle bundle  = data.getBundleExtra("options");
            if(bundle != null) routeListInterface.resetList(bundle);
        }
    }
//==================================================================================================
    //back button override

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Override for the back button. Only finishes activity when the back button is pressed twice
     * within 2 seconds.
     */
    @Override
    public void onBackPressed() {

        if(lastClicked == null){
            lastClicked = System.currentTimeMillis();
            makeToastLong(context, getString(R.string.back_confirm));
        } else if(System.currentTimeMillis() - lastClicked < 2000){
            finish();
        } else {
            lastClicked = System.currentTimeMillis();
            makeToastLong(context, getString(R.string.back_confirm));
        }
    }
}
