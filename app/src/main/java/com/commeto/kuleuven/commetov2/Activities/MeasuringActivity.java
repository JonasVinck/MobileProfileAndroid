package com.commeto.kuleuven.commetov2.Activities;

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
import android.content.SharedPreferences;
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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.commeto.kuleuven.commetov2.Fragments.MeasuringGraphFragment;
import com.commeto.kuleuven.commetov2.Fragments.MeasuringMapFragment;
import com.commeto.kuleuven.commetov2.Adapters.MeasuringPagerAdapter;
import com.commeto.kuleuven.commetov2.Listeners.UnderlineButtonListener;
import com.commeto.kuleuven.commetov2.R;
import com.commeto.kuleuven.commetov2.Services.MeasuringService;
import com.commeto.kuleuven.commetov2.Support.InternalIO;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static com.commeto.kuleuven.commetov2.Support.InternalIO.backupExists;
import static com.commeto.kuleuven.commetov2.Support.InternalIO.deleteFromCache;
import static com.commeto.kuleuven.commetov2.Support.InternalIO.readFromCache;
import static com.commeto.kuleuven.commetov2.Support.Static.makeToastLong;
import static com.commeto.kuleuven.commetov2.Support.Static.timeFormat;
import static com.commeto.kuleuven.commetov2.Support.Static.timeToStringWithSeconds;

/**
 * Created by Jonas on 15/03/2018.
 */

public class MeasuringActivity extends AppCompatActivity{
//==================================================================================================
    //broadcast receiver

    public class MeasurementBroadcastReceiver extends BroadcastReceiver {
        private static final String tag = "MeasurementUpdates";

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
                    intent.getDoubleExtra("bearing", 0),
                    intent.getDoubleExtra("altitude", -1),
                    intent.getDoubleExtra("result", -1),
                    intent.getDoubleExtra("lightResult", -1)
            );
        }
    }
//==================================================================================================
    //class specs

    private String musicPackage;
    private static final String CMDPAUSE = "pause";
    private static final String CMDPLAY = "play";
    private static final String CMDPREVIOUS = "previous";
    private static final String CMDNEXT = "next";
    private static final String SERVICECMD = "com.android.music.musicservicecommand";
    private static final String CMDNAME = "command";
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ((TextView) findViewById(R.id.title)).setText(intent.getStringExtra("track"));
        }
    };

    private ViewPager pager;
    private MeasuringPagerAdapter adapter;

    private Context context;
    private boolean measuring;
    private Long start;

    private boolean changed;
    private MapboxMap mapboxMap;

    private AudioManager audioManager;

    private MeasuringActivity.MeasurementBroadcastReceiver measurementBroadcastReceiver;
    private MeasuringService service;
    private boolean bound;
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            MeasuringService.MeasuringBinder measuringBinder =
                    (MeasuringService.MeasuringBinder) iBinder;
            service = measuringBinder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            service = null;
            bound = false;
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

        measurementBroadcastReceiver = new MeasuringActivity.MeasurementBroadcastReceiver();
        this.registerReceiver(
                measurementBroadcastReceiver,
                new IntentFilter("MeasurementUpdate")
        );

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.music.metachanged");
        registerReceiver(receiver, intentFilter);

        //Mapbox.getInstance(this, getResources().getString(R.string.mapbox_key));

        List<Fragment> fragments = new LinkedList<>();

        if(measuring){
            fragments.add(MeasuringGraphFragment.newInstance(0, "Trilling", true));
        }
        fragments.add(MeasuringGraphFragment.newInstance(1, "Verlichting", true));
        fragments.add(MeasuringGraphFragment.newInstance(2, "Hoogtverschil", false));
        //fragments.add(MeasuringMapFragment.newInstance(3, "Kaart", context));

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

        adapter = new MeasuringPagerAdapter(getSupportFragmentManager(), fragments);
        (pager = findViewById(R.id.pager)).setAdapter(adapter);
        pager.setOffscreenPageLimit(4);
    }

    @Override
    public void onStart(){
        super.onStart();

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

    @Override
    public void onBackPressed() {

        finishAlert();
    }

//==================================================================================================
    //permission check

    @TargetApi(Build.VERSION_CODES.M)
    private void checkForPermisions(){

        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            String[] permissions = {
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
            requestPermissions(permissions, 123);
        } else{
            if(backupExists(context)) backupAlert();
            else startMeasuring(null, null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NotNull String permissions[],
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

    private void end(){

        unbindService(serviceConnection);
        unregisterReceiver(measurementBroadcastReceiver);
        finish();
    }



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

    public void previous(View view){

        try {
            Intent intent = new Intent(SERVICECMD);
            intent.putExtra(CMDNAME , CMDPREVIOUS);
            MeasuringActivity.this.sendBroadcast(intent);
        } catch (Exception e){
            InternalIO.writeToLog(context, e);
        }
    }

    public void playPause(View view){

        try {
            findViewById(R.id.play_pause_button).setBackgroundResource(audioManager.isMusicActive() ?
                            android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play
            );
            Intent intent = new Intent(SERVICECMD);
            intent.putExtra(CMDNAME , audioManager.isMusicActive() ? CMDPAUSE : CMDPLAY);
            MeasuringActivity.this.sendBroadcast(intent);
        } catch (Exception e){
            InternalIO.writeToLog(context, e);
        }
    }

    public void next(View view){
        try {
            Intent intent = new Intent(SERVICECMD);
            intent.putExtra(CMDNAME , CMDNEXT);
            MeasuringActivity.this.sendBroadcast(intent);
        } catch (Exception e){
            InternalIO.writeToLog(context, e);
        }
    }

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
