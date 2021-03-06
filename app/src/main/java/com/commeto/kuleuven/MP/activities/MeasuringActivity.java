package com.commeto.kuleuven.MP.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.commeto.kuleuven.MP.fragments.MeasuringGraphFragment;
import com.commeto.kuleuven.MP.adapters.MeasuringPagerAdapter;
import com.commeto.kuleuven.MP.fragments.MeasuringMapFragment;
import com.commeto.kuleuven.MP.listeners.UnderlineButtonListener;
import com.commeto.kuleuven.MP.R;
import com.commeto.kuleuven.MP.services.MeasuringService;
import com.commeto.kuleuven.MP.support.InternalIO;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;

import static com.commeto.kuleuven.MP.support.InternalIO.backupExists;
import static com.commeto.kuleuven.MP.support.InternalIO.deleteFromCache;
import static com.commeto.kuleuven.MP.support.InternalIO.readFromCache;
import static com.commeto.kuleuven.MP.support.Static.makeToastLong;
import static com.commeto.kuleuven.MP.support.Static.timeToStringWithSeconds;

/**
 * Created by Jonas on 15/03/2018.
 *
 * Core activity for measuring.
 *
 * Linked services:
 *  - MeasuringService
 *
 * Broadcast receivers:
 *  - MeasurementBroadcastReceiver
 *  - BroadcastReceiver to get current playing track
 */

public class MeasuringActivity extends AppCompatActivity{
//==================================================================================================
    //constants

    private static final String CMDPAUSE = "pause";
    private static final String CMDPLAY = "play";
    private static final String CMD_PREVIOUS = "previous";
    private static final String CMD_NEXT = "next";
    private static final String SERVICE_CMD = "com.android.music.musicservicecommand";
    private static final String CMD_NAME = "command";
//==================================================================================================
    //broadcast receivers

    /**
     * BroadcastReceiver class to receive the data from the MeasuringService.
     *
     * Fills in all fields of the measuring_layout with the data from the MeasuringService.
     */
    public class MeasurementBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent){

            if(!changed) {
                changed = true;
                start = System.currentTimeMillis();
                ((TextView) findViewById(R.id.gpsMessage)).setText(getString(R.string.gps_conected));
                ((TextView) findViewById(R.id.speed_unit)).setText(getString(R.string.kmh));
                ((TextView) findViewById(R.id.distance_unit)).setText(getString(R.string.m));
            }
            double distance;
            distance = intent.getDoubleExtra("distance", 0);
            if(distance > 1000){
                distance = distance / 1000;
                ((TextView) findViewById(R.id.distance_unit)).setText(getString(R.string.km));
            }
            float speed = intent.getFloatExtra("speed", 0) * 3.6f;

            ((TextView) findViewById(R.id.distance)).setText(
                    String.format(Locale.getDefault(), "%.2f", distance)
            );
            ((TextView) findViewById(R.id.momentSpeed)).setText(
                    String.format(Locale.getDefault(), "%.2f", speed)
            );
            if(measuring){
                if(speed < 10.0f || speed > 25.0f){
                    findViewById(R.id.top).setBackgroundColor(getResources().getColor(R.color.red));
                } else{
                    findViewById(R.id.top).setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                }
            }

            ((TextView) findViewById(R.id.time)).setText(
                    timeToStringWithSeconds(System.currentTimeMillis() - start)
            );

            adapter.append(
                    intent.getDoubleExtra("latitude", -1),
                    intent.getDoubleExtra("longitude", -1),
                    intent.getDoubleExtra("altitude", -1),
                    intent.getDoubleExtra("result", -1),
                    intent.getDoubleExtra("lightResult", -1)
            );
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ((TextView) findViewById(R.id.title)).setText(intent.getStringExtra("track"));
        }
    };
//==================================================================================================
    //class specs

    private ViewPager pager;
    private MeasuringPagerAdapter adapter;

    private Context context;
    private boolean measuring;
    private Long start;

    private boolean changed;

    private AudioManager audioManager;

    private MeasuringActivity.MeasurementBroadcastReceiver measurementBroadcastReceiver;
    private MeasuringService service;
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            MeasuringService.MeasuringBinder measuringBinder =
                    (MeasuringService.MeasuringBinder) iBinder;
            service = measuringBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            service = null;
        }
    };
//=================================================================================================
    //lifecycle methods

    @Override
    public void onCreate(Bundle bundle){

        super.onCreate(bundle);
        setContentView(R.layout.activity_measuring_portrait);
        context = getApplicationContext();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        changed = false;

        measuring = getIntent().getBooleanExtra("measuring", false);

        //Register the receiver to get the data.
        measurementBroadcastReceiver = new MeasuringActivity.MeasurementBroadcastReceiver();
        this.registerReceiver(
                measurementBroadcastReceiver,
                new IntentFilter("MeasurementUpdate")
        );

        //Initiate AudioManager to control music vpolume.
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.music.metachanged");
        registerReceiver(receiver, intentFilter);

        findViewById(R.id.end).setOnTouchListener(new UnderlineButtonListener(context));
        findViewById(R.id.end).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAlert();
            }
        });
        findViewById(R.id.volume_up).setOnTouchListener(new UnderlineButtonListener(context));
        findViewById(R.id.next).setOnTouchListener(new UnderlineButtonListener(context));
        findViewById(R.id.play_pause).setOnTouchListener(new UnderlineButtonListener(context));
        findViewById(R.id.previous).setOnTouchListener(new UnderlineButtonListener(context));
        findViewById(R.id.volume_down).setOnTouchListener(new UnderlineButtonListener(context));

        HashMap<String, Fragment> fragments = new HashMap<>();
        String[] titles, titleOptions = getResources().getStringArray(R.array.measuring_fragments);

        //Which fragments are shown depends on whether the user is measuring or not.
        //Vibration graph not shown unless user is measuring.
        if(measuring){
            titles = new String[4];
            fragments.put(titleOptions[0], MeasuringGraphFragment.newInstance(true));
            titles[0] = titleOptions[0];
            titles[1] = titleOptions[1];
            titles[2] = titleOptions[2];
            titles[3] = titleOptions[3];
        } else {
            titles = new String[3];
            titles[0] = titleOptions[1];
            titles[1] = titleOptions[2];
            titles[2] = titleOptions[3];
        }
        fragments.put(titleOptions[1], MeasuringGraphFragment.newInstance(true));
        fragments.put(titleOptions[2], MeasuringGraphFragment.newInstance(false));
        fragments.put(titleOptions[3], MeasuringMapFragment.newInstance(context));

        //Getting the adapter for the extra view and filling it.
        adapter = new MeasuringPagerAdapter(getSupportFragmentManager(), fragments, titles);
        pager = findViewById(R.id.pager);
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(4);

        if(backupExists(context)) backupAlert();
    }

    @Override
    public void onStart(){
        super.onStart();

        //Check if gps is turned on.
        try {
            if(!((LocationManager) context.getSystemService(Context.LOCATION_SERVICE))
                    .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Intent gpsOptionsIntent = new Intent(
                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(gpsOptionsIntent);
            }
        } catch (Exception e){
            InternalIO.writeToLog(context, e);
            makeToastLong(context, "er ging iets mis");
        }
        checkForPermisions();

        pager.setCurrentItem(0, true);
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
    }
//==================================================================================================
    //back back button override

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
     * onBackPressed override.
     */
    @Override
    public void onBackPressed() {

        finishAlert();
    }

//==================================================================================================
    //permission check

    /**
     * Checking for permissions to use the gps is only necessary starting from API level 19.
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void checkForPermisions(){

        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            String[] permissions = {
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
            requestPermissions(permissions, 123);
        } else startMeasuring(null, null);
    }

    /**
     * If permissions are not granted, finish activity. If permissions are granted first checks for
     * possible backup. If none exists, starts measuring, otherwise ask user to reload backup.
     *
     * @param requestCode  The requestCode for the permission check.
     * @param permissions  The permissions being asked.
     * @param grantResults The permissions granted.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NotNull String[] permissions,
                                           @NotNull int[] grantResults) {

        if(requestCode == 123) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(backupExists(context)) backupAlert();
                else startMeasuring(null, null);
            } else {
                finish();
            }
        }
    }
//==================================================================================================
    //private methods

    /**
     * Shows a dialog to confirm the end of the measuring.
     */
    private void finishAlert(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rit beëindigen?");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                end();
                if(dialog != null){
                    dialog.dismiss();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(dialog != null){
                    dialog.dismiss();
                }
            }
        });

        builder.show();
    }

    /**
     * Shows a dialog asking if the backup should be restored. If the backup has to be restored,
     * read the info and backup file from memory and reconstruct the MeasurementArray. Otherwise
     * delete the backup.
     */
    private void backupAlert(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Backup van een rit gevonden, wilt u vorige rit herstellen?");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    startMeasuring(
                            readFromCache(context, "info"),
                            readFromCache(context, "backup"));
                } catch (Exception e){
                    InternalIO.writeToLog(context, e);
                    makeToastLong(context, "Er ging iets mis bij het laden van de backup.");
                }
                if(dialog != null){
                    dialog.dismiss();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteFromCache(context, "info");
                deleteFromCache(context, "backup");
                startMeasuring(null, null);
                if(dialog != null){
                    dialog.dismiss();
                }
            }
        });

        builder.show();
    }

    /**
     * Unbind the MeasuringService and unregister the BroadcastReceiver before finishing.
     */
    private void end(){

        unbindService(serviceConnection);
        unregisterReceiver(measurementBroadcastReceiver);
        finish();
    }

    /**
     * Start the services necessary to start measuring and pass the needed values to them.
     *
     * @param info   The possibly existing backup info about a ride.
     * @param backup The possibly existing backup data fom a ride.
     */
    private void startMeasuring(String info, String backup){

        boolean keep = getIntent().getBooleanExtra("keep", false);
        boolean offroad = getIntent().getBooleanExtra("offroad", false);
        Intent intent = new Intent(context, MeasuringService.class)
                .putExtra("keep", keep)
                .putExtra("offroad", offroad)
                .putExtra("type",
                        getIntent().hasExtra("type") ?
                                getIntent().getStringExtra("type") : "plezier");
        if(info != null && backup != null){
            intent.putExtra("info", info.split(","))
                    .putExtra("backup", backup);
        }
        bindService(
                intent,
                serviceConnection, Service.BIND_AUTO_CREATE
        );
    }
//==================================================================================================
    //button actions
/*
    public void rotate(View view){
        setRequestedOrientation(getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ?
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ?
            R.layout.activity_measuring_portrait : R.layout.activity_measuring_landscape);
    }
*/

    /**
     * Method to turn down the volume.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void volumeDown(View view){

        try {
            audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) - 1,
                    0
            );
        } catch (Exception e){
            InternalIO.writeToLog(context, e);
        }
    }

    /**
     * Method to go to the previous song. Broadcasts a message to the running music player.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void previous(View view){

        try {
            Intent intent = new Intent(SERVICE_CMD);
            intent.putExtra(CMD_NAME, CMD_PREVIOUS);
            MeasuringActivity.this.sendBroadcast(intent);
        } catch (Exception e){
            InternalIO.writeToLog(context, e);
        }
    }

    /**
     * Method to paly or pause the music. Broadcasts a message to the running music player.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void playPause(View view){

        try {
            findViewById(R.id.play_pause_button).setBackgroundResource(audioManager.isMusicActive() ?
                            android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play
            );
            Intent intent = new Intent(SERVICE_CMD);
            intent.putExtra(CMD_NAME, audioManager.isMusicActive() ? CMDPAUSE : CMDPLAY);
            MeasuringActivity.this.sendBroadcast(intent);
        } catch (Exception e){
            InternalIO.writeToLog(context, e);
        }
    }

    /**
     * Method to go to the next song. Broadcasts a message to the running music player.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void next(View view){
        try {
            Intent intent = new Intent(SERVICE_CMD);
            intent.putExtra(CMD_NAME, CMD_NEXT);
            MeasuringActivity.this.sendBroadcast(intent);
        } catch (Exception e){
            InternalIO.writeToLog(context, e);
        }
    }


    /**
     * Method to turn up the volume.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void volumeUp(View view){

        try {
            audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) + 1,
                    0
            );
        } catch (Exception e){
            InternalIO.writeToLog(context, e);
        }
    }
//==================================================================================================
}
