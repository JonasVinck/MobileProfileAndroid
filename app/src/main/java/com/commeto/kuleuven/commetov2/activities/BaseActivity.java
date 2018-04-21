package com.commeto.kuleuven.commetov2.activities;

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

import com.commeto.kuleuven.commetov2.adapters.BasePagerAdapter;
import com.commeto.kuleuven.commetov2.dataClasses.HTTPResponse;
import com.commeto.kuleuven.commetov2.dialogs.BaseConfirmDialogBuilder;
import com.commeto.kuleuven.commetov2.dialogs.BaseEditDialogBuilder;
import com.commeto.kuleuven.commetov2.interfaces.AsyncResponseInterface;
import com.commeto.kuleuven.commetov2.interfaces.RouteListInterface;
import com.commeto.kuleuven.commetov2.interfaces.BaseDialogInterface;
import com.commeto.kuleuven.commetov2.interfaces.SyncInterface;
import com.commeto.kuleuven.commetov2.listeners.MenuIconUnderlineListener;
import com.commeto.kuleuven.commetov2.R;
import com.commeto.kuleuven.commetov2.sqlSupport.LocalDatabase;
import com.commeto.kuleuven.commetov2.sqlSupport.LocalRoute;
import com.commeto.kuleuven.commetov2.services.SyncService;
import com.commeto.kuleuven.commetov2.services.SyncServiceConnection;
import com.commeto.kuleuven.commetov2.support.ExternalIO;
import com.commeto.kuleuven.commetov2.support.InternalIO;
import com.commeto.kuleuven.commetov2.views.LockableViewPager;

import java.util.List;

import static com.commeto.kuleuven.commetov2.http.HTTPStatic.convertInputStreamToString;
import static com.commeto.kuleuven.commetov2.support.NotifyStatic.postNotification;
import static com.commeto.kuleuven.commetov2.support.Static.isNetworkAvailable;
import static com.commeto.kuleuven.commetov2.support.Static.makeToastLong;
import static com.commeto.kuleuven.commetov2.support.Static.scaleMenuIcon;
import static com.commeto.kuleuven.commetov2.support.Static.tryLogin;

/**
 * Created by Jonas on 1/03/2018.
 */

public class BaseActivity extends AppCompatActivity{

    private String CALIBRATION;

    private AsyncResponseInterface loginSucces = new AsyncResponseInterface() {
        @Override
        public void processFinished(HTTPResponse response) {
            if(response != null && response.getResponseCode() == 401){
                startActivity(new Intent(context, LoginActivity.class));
                try {
                    finish();
                } catch (NullPointerException e){
                    InternalIO.writeToLog(context, e);
                }
            } else if(response != null && response.getResponseCode() == -2){
                postNotification(context, getString(R.string.offline_mode), getString(R.string.no_internet_message));
            }
        }
    };

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

    private Long lastClicked;

    private Context context;
    private LockableViewPager basePager;
    private BasePagerAdapter basePagerAdapter;
    private RouteListInterface routeListInterface;

    private String toWrite;
    private DialogInterface dialog;

    private Drawable resetDrawable;
    private ImageView currentIcon;
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

    @Override
    public void onCreate(Bundle bundle){
        CALIBRATION = getString(R.string.preferences_calibration);

        super.onCreate(bundle);
        setContentView(R.layout.activity_base);
        this.context = getApplicationContext();
        toWrite = "";
        preferences  = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        lastClicked = null;
        syncServiceConnection.setSyncInterface(syncInterface);
        syncIntent = new Intent(this, SyncService.class);
        BaseActivity.this.bindService(syncIntent, syncServiceConnection, BIND_AUTO_CREATE);

        resetDrawable = null;
        currentIcon = null;
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

        int value = (scaleMenuIcon() / 2);
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

        basePager = findViewById(R.id.base_pager);
        basePagerAdapter = new BasePagerAdapter(getSupportFragmentManager());
        routeListInterface = basePagerAdapter.getRouteListInterface();
        basePager.setAdapter(basePagerAdapter);
        basePager.setOffscreenPageLimit(4);
        toStart(null);

        if(preferences.getBoolean("auto_sync_option", true)) sync(null);
    }

    @Override
    public void onStart(){

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
        BaseActivity.this.unbindService(syncServiceConnection);
        if(dialog != null) dialog.dismiss();
    }
//==================================================================================================
    //button actions

    public void toStart(View view){
        basePager.setCurrentItem(2);
        switchActive((ImageView) findViewById(R.id.start_icon), null, R.drawable.logo_svg_clicked);
        startListener.setEnable(false);
        resetListener = startListener;
    }

    public void toProfile(View view){
        basePager.setCurrentItem(1);
        switchActive((ImageView) findViewById(R.id.profile_icon), (TextView) findViewById(R.id.user_text), R.drawable.ic_user_clicked);
        profileListener.setEnable(false);
        resetListener = profileListener;
    }

    public void toList(View view){
        basePager.setCurrentItem(3);
        switchActive((ImageView) findViewById(R.id.list_icon), (TextView) findViewById(R.id.list_text), R.drawable.ic_list_clicked);
        listListener.setEnable(false);
        resetListener = listListener;
    }

    public void toMap(View view){
        basePager.setCurrentItem(0);
        switchActive((ImageView) findViewById(R.id.map_icon), (TextView) findViewById(R.id.map_text), R.drawable.ic_map_clicked);
        mapListener.setEnable(false);
        resetListener = mapListener;
    }

    public void toSettings(View view){
        basePager.setCurrentItem(4);
        switchActive((ImageView) findViewById(R.id.settings_icon), (TextView) findViewById(R.id.settings_text), R.drawable.ic_settings_clicked);
        settingsListener.setEnable(false);
        resetListener = settingsListener;
    }

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

                    startActivity(intent);
                }

                if(dialogInterface != null) dialogInterface.dismiss();
            }
        });
        builder.show();
    }
    public void logout(View view){
        preferences.edit().clear().apply();

        Intent intent = new Intent(context, CheckLoginActivity.class);
        setResult(RESULT_OK);
        startActivity(intent);
        finish();
    }

    public void relogin(View view){
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra("certificate", true);
        startActivity(intent);
        setResult(RESULT_OK);
        finish();
    }

    public void recalibrate(View view){
        Intent intent = new Intent(this, Callibration.class);
        startActivityForResult(intent, 0);
    }

    public void resetBaseUrl(View view){

        BaseDialogInterface dialogInterface = new BaseDialogInterface() {
            @Override
            public void confirm(String string) {

                preferences.edit().putString("baseUrl", getString(R.string.hard_coded_ip)).apply();
                ((TextView) findViewById(R.id.ip)).setText(getString(R.string.hard_coded_ip));
            }
        };

        BaseConfirmDialogBuilder confirmDialog = new BaseConfirmDialogBuilder(
                this,
                "Huidig: " + preferences.getString("baseUrl", "") + ", vervangen?",
                dialogInterface
        );

        confirmDialog.show();
    }

    public void setBaseUrl(View view){

        BaseDialogInterface dialogInterface = new BaseDialogInterface() {
            @Override
            public void confirm(String string) {
                if(!string.equals("")) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("baseUrl", string);
                    editor.apply();
                }
                ((TextView) findViewById(R.id.ip)).setText(string);
            }
        };

        BaseEditDialogBuilder editDialog = new BaseEditDialogBuilder(
                this,
                "Huidig:\n" + preferences.getString("baseUrl", getString(R.string.hard_coded_ip)),
                preferences.getString("baseUrl", getString(R.string.hard_coded_ip)),
                dialogInterface
        );

        editDialog.show();
    }

    public void resetBaseSocket(View view){

        BaseDialogInterface dialogInterface = new BaseDialogInterface() {
            @Override
            public void confirm(String string) {

                preferences.edit().putString("socket", getString(R.string.hard_coded_socket)).apply();
                ((TextView) findViewById(R.id.socket)).setText(getString(R.string.hard_coded_socket));
            }
        };

        BaseConfirmDialogBuilder confirmDialog = new BaseConfirmDialogBuilder(
                this,
                "Huidig:" + preferences.getString("socket", getString(R.string.hard_coded_socket)) + ", vervangen?",
                dialogInterface
        );

        confirmDialog.show();
    }

    public void setBaseSocket(View view){

        BaseDialogInterface dialogInterface = new BaseDialogInterface() {
            @Override
            public void confirm(String string) {
                if(!string.equals("")) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("socket", string);
                    editor.apply();
                }
                ((TextView) findViewById(R.id.socket)).setText(string);
            }
        };

        BaseEditDialogBuilder editDialog = new BaseEditDialogBuilder(
                this,
                "Huidig:\n" + preferences.getString("socket", getString(R.string.hard_coded_socket)),
                preferences.getString("socket", getString(R.string.hard_coded_socket)),
                dialogInterface
        );

        editDialog.show();
    }

    public void exportLog(View view){
        try {
            toWrite = convertInputStreamToString(InternalIO.getInputStream(context, "log"));
        } catch (Exception e){
            toWrite = "probleem bij log exporteren";
        }
        ExternalIO.createFile(this, "text/plain", "log.txt");
    }

    public void deleteLog(View view){
        InternalIO.deleteLog(context);
    }

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

    public void search(View view){
        findViewById(R.id.search_bar_container).setVisibility(View.VISIBLE);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endSearch(view);
            }
        });
    }

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

    public void mapOption(View view){

        int visibility = findViewById(R.id.map_option_list).getVisibility();
        findViewById(R.id.map_option_list).setVisibility(visibility == View.VISIBLE ?
                View.GONE : View.VISIBLE
        );
    }

    public void sort(View view){
        startActivityForResult(
                new Intent(this, FilterSortActivity.class).putExtra("options", routeListInterface.getPrevious()),
                581
        );
    }

    public void searchList(View view){
        findViewById(R.id.list_search_bar_container).setVisibility(View.VISIBLE);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSearchList(view);
            }
        });
    }

    public void hideSearchList(View view){
        findViewById(R.id.list_search_bar_container).setVisibility(View.GONE);
        routeListInterface.setSearch();
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchList(view);
            }
        });
    }

    public void autoUploadSetting(View view){
        ((Switch) findViewById(R.id.auto_upload_option)).toggle();
    }

    public void autoSyncSetting(View view){
        ((Switch) findViewById(R.id.auto_sync_option)).toggle();
    }

    public void exportFullSetting(View view){

        ((Switch) view.findViewById(R.id.export_full_switch)).toggle();
    }

    public void debugSetting(View view){

        ((Switch) view.findViewById(R.id.debug_switch)).toggle();
    }

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

    public void disableSync(View view){
        makeToastLong(context, getString(R.string.already_syncing));
    }

    public void crash(View view){
        int i = 1 / 0;
    }
//==================================================================================================
    //debug

    public void setAllNotSent(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.warning));
        builder.setMessage(getString(R.string.set_all_not_sent_message));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                LocalDatabase localDatabase;
                List<LocalRoute> routes = (localDatabase = LocalDatabase.getInstance(context)).localRouteDAO().debug();
                for(LocalRoute route: routes){
                    route.setSent(false);
                    localDatabase.localRouteDAO().update(route);
                }
                if(dialogInterface != null) dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                makeToastLong(context, "Crisis averted...");
                if(dialogInterface != null) dialogInterface.dismiss();
            }
        });
        builder.show();
    }
//==================================================================================================
    //private methods

    private void switchActive(ImageView newCurrentIcon, TextView newCurrentText, int iconId){

        try {
            currentIcon.setImageDrawable(resetDrawable);
            if(currentText != null) currentText.setTextColor(getResources().getColor(R.color.logo_text));
        } catch (NullPointerException e){

        } finally {
            resetDrawable = newCurrentIcon.getDrawable();
            currentIcon = newCurrentIcon;
            currentText = newCurrentText;
            resetListener.setEnable(true);

            currentIcon.setImageDrawable(getResources().getDrawable(iconId));
            if(currentText != null) currentText.setTextColor(getResources().getColor(R.color.accent));
        }
    }
//==================================================================================================
    //activity result

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){

        routeListInterface.resetList(null);

        if(requestCode == 0){
            boolean test = preferences.getInt(CALIBRATION, 0) > 45;
            findViewById(R.id.can_measure).setVisibility(test ? View.VISIBLE : View.GONE);
            findViewById(R.id.can_not_measure).setVisibility(test ? View.GONE : View.VISIBLE);
        }
        if(requestCode == 580 && resultCode == RESULT_OK){
            finish();
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
