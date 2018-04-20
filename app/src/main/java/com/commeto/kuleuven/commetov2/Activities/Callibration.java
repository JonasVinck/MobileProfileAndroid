package com.commeto.kuleuven.commetov2.Activities;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.commeto.kuleuven.commetov2.Listeners.UnderlineButtonListener;
import com.commeto.kuleuven.commetov2.R;
import com.commeto.kuleuven.commetov2.Services.SensorService;
import com.commeto.kuleuven.commetov2.Services.SensorServiceConnection;

import java.util.LinkedList;
import java.util.Locale;

import static com.commeto.kuleuven.commetov2.Support.Static.makeToastLong;

/**
 * Created by Jonas on 26/03/2018.
 */

public class Callibration extends AppCompatActivity{

//==================================================================================================
//class specs

    private Context context;
    private LinkedList<float[]> values;
    private int max;

    private SensorServiceConnection connection;
    private Runnable calibrator;
    private Handler handler;
//==================================================================================================
    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.activity_callibration);
        context = getApplicationContext();
        values = null;
        connection = new SensorServiceConnection();
        calibrator = new Runnable() {
            @Override
            public void run() {
                values = connection.getAccelerometerData();

                int maxtemp = getMax();
                if(maxtemp > max){
                    max = maxtemp;

                    if (values != null){
                        getSharedPreferences("commeto", MODE_PRIVATE)
                                .edit()
                                .putInt("calibration", max)
                                .apply();
                    }

                    if(max > 45){
                        findViewById(R.id.calibrated).setBackgroundColor(
                                getResources().getColor(R.color.green)
                        );
                        ((TextView) findViewById(R.id.calibration_message)).setText(
                                getResources().getString(R.string.good_message)
                        );
                    } else {
                        findViewById(R.id.calibrated).setBackgroundColor(
                                getResources().getColor(R.color.red)
                        );
                        ((TextView) findViewById(R.id.calibration_message)).setText(
                                getResources().getString(R.string.bad_message)
                        );
                    }

                    handler.postDelayed(calibrator, 1000);
                }
            }
        };
        handler = new Handler();
    }

    @Override
    public void onStart(){

        findViewById(R.id.confirm).setOnTouchListener(new UnderlineButtonListener(context));
        findViewById(R.id.recallibrate).setOnTouchListener(new UnderlineButtonListener(context));

        Intent service = new Intent(this, SensorService.class);
        bindService(service, connection, BIND_AUTO_CREATE);

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
        unbindService(connection);
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

        if(values == null){
            makeToastLong(context, "niet gecallibreerd");
        } else {
            end();
        }
    }
//==================================================================================================
    //onclick actions

    public void confirm(View view){
        if(values == null) {
            makeToastLong(context, "niet gecallibreerd");
        } else{
            end();
        }
    }

    public void calibrate(View view){

        if (connection.isBound()) {
            handler.postDelayed(
                    calibrator,
                    1000
            );
        }
    }

//==================================================================================================
    //private methods

    private int getMax(){

        float max = 0;
        int i;

        for(float[] array: values){
            for(i = 0; i < array.length; i++) if(max < array[i]) max = array[i];
        }

        return Math.round(max);
    }

    private void end(){
        handler.removeCallbacks(calibrator);
        finish();
    }
//==================================================================================================
}