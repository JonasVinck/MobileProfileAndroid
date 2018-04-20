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
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.commeto.kuleuven.commetov2.R;
import com.commeto.kuleuven.commetov2.Services.MeasuringService;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Jonas on 1/03/2018.
 */

public class MeasuringActivityTest extends AppCompatActivity {

//==================================================================================================
    //broadcast receiver

    public class MeasurementBroadcastReceiver extends BroadcastReceiver{

        private static final String tag = "MeasurementUpdates";

        @Override
        public void onReceive(Context context, Intent intent){

            ((TextView) findViewById(R.id.distance)).setText(intent.getStringExtra("distance"));
            ((TextView) findViewById(R.id.momentSpeed)).setText(intent.getStringExtra("speed"));
            ((TextView) findViewById(R.id.latitude)).setText(intent.getStringExtra("latitude"));
            ((TextView) findViewById(R.id.longitude)).setText(intent.getStringExtra("longitude"));
            ((TextView) findViewById(R.id.vbrX)).setText(intent.getStringExtra("vbrX"));
            ((TextView) findViewById(R.id.vbrY)).setText(intent.getStringExtra("vbrY"));
            ((TextView) findViewById(R.id.vbrZ)).setText(intent.getStringExtra("vbrZ"));
        }
    }
//==================================================================================================
    //class specs

    private Context context;

    private MeasurementBroadcastReceiver measurementBroadcastReceiver;
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

        alert();
    }

//==================================================================================================
    //lifecycle methods

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_measuring_test);
        context = getApplicationContext();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        measurementBroadcastReceiver = new MeasurementBroadcastReceiver();
        this.registerReceiver(measurementBroadcastReceiver, new IntentFilter("MeasurementUpdate"));

        findViewById(R.id.end).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert();
            }
        });

        service = null;
        bound = false;
    }

    @Override
    public void onStart() {

        super.onStart();

        checkForPermisions();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
            startMeasuring();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String permissions[], @NotNull int[] grantResults) {
        switch (requestCode){
            case 123:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startMeasuring();
                } else {
                    finish();
                }
                break;
            default:
                break;
        }
    }
//==================================================================================================
    //private methods

    private void alert(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Geef deze rit een naam.");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!input.getText().toString().equals("")){

                    service.giveName(input.getText().toString());
                    end();

                    if(dialog != null){
                    dialog.dismiss();
                    }
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

    private void end(){

        unbindService(serviceConnection);
        unregisterReceiver(measurementBroadcastReceiver);
        setResult(RESULT_OK);
        finish();
    }

    private void startMeasuring(){

        bindService(new Intent(context, MeasuringService.class), serviceConnection, Service.BIND_AUTO_CREATE);
    }
}
