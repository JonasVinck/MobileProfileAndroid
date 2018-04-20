package com.commeto.kuleuven.commetov2.Services;

import java.util.LinkedList;
import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;

import com.commeto.kuleuven.commetov2.Activities.RideDisplayActivity;
import com.commeto.kuleuven.commetov2.DataClasses.Measurement;
import com.commeto.kuleuven.commetov2.DataClasses.MeasurementArray;
import com.commeto.kuleuven.commetov2.Exceptions.NoDistanceException;
import com.commeto.kuleuven.commetov2.Support.InternalIO;

import static com.commeto.kuleuven.commetov2.Support.InternalIO.deleteFromCache;
import static com.commeto.kuleuven.commetov2.Support.Static.makeToastLong;

/**
 * Created by Jonas on 1/03/2018.
 */

public class MeasuringService extends IntentService{

//==================================================================================================
    //location listener

    class MeasuringLocationListener implements LocationListener{

        @Override
        public void onLocationChanged(Location location) {

            if(connection.isBound()) {
                if (!started){
                    previousLocation = location;
                    started = true;
                }

                Intent broadcast = new Intent();
                Measurement measurement;
                if(keep) {
                    LinkedList<float[]> temp = connection.getAccelerometerData();
                    measurement = new Measurement(
                            location.getLatitude(),
                            location.getLongitude(),
                            location.getAltitude(),
                            temp,
                            connection.getLightsensorData(),
                            location.getSpeed(),
                            location.distanceTo(previousLocation),
                            location.getAccuracy()
                    );

                    try {
                        broadcast.putExtra("vbrX", Float.toString(temp.get(0)[0]));
                        broadcast.putExtra("vbrY", Float.toString(temp.get(0)[1]));
                        broadcast.putExtra("vbrZ", Float.toString(temp.get(0)[2]));
                    } catch (Exception e){}
                } else{
                    measurement = new Measurement(
                            location.getLatitude(),
                            location.getLongitude(),
                            location.getAltitude(),
                            null,
                            connection.getLightsensorData(),
                            location.getSpeed(),
                            location.distanceTo(previousLocation),
                            location.getAccuracy()
                    );
                }

                broadcast.putExtra("longitude", location.getLongitude());
                broadcast.putExtra("latitude", location.getLatitude());
                broadcast.putExtra("altitude", location.getAltitude());
                broadcast.putExtra("bearing", location.getBearing());
                broadcast.putExtra("result", measurement.getAccellerationResult());
                broadcast.putExtra("lightResult", measurement.getLightResult());
                broadcast.putExtra("speed", location.getSpeed());
                broadcast.putExtra("distance", measurementArray.getDistance());
                broadcast.putExtra("time", measurementArray.getTime());

                broadcast.setAction("MeasurementUpdate");
                sendBroadcast(broadcast);

                InternalIO.writeToCache(context, "info", measurementArray.toString());
                InternalIO.appendToCache(context, "backup", measurement.toString() + "\n");

                measurementArray.add(measurement, location.getSpeed() > 0.5);
                previousLocation = location;
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    }
//==================================================================================================
    //handler

    @Override
    public void onHandleIntent(Intent intent){
    }
//==================================================================================================
    //binder

    public class MeasuringBinder extends Binder{

        public MeasuringService getService(){
            return MeasuringService.this;
        }
    }

    @Override
    public MeasuringBinder onBind(Intent intent){

        keep = intent.getBooleanExtra("keep", false);
        type = intent.getStringExtra("type");
        if(type == null) type = "offroad";
        if(type.equals("")) type = "offroad";

        if(intent.getStringArrayExtra("info") != null && intent.getStringExtra("backup") != null){
            measurementArray = new MeasurementArray(intent.getStringArrayExtra("info"), intent.getStringExtra("backup"));
        }

        return MeasuringBinder;
    }
//==================================================================================================
    //class spec

    private final MeasuringService.MeasuringBinder MeasuringBinder = new MeasuringBinder();

    private Context context;

    private boolean keep;
    private String type;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location previousLocation;

    private boolean started;
    private MeasurementArray measurementArray;

    private SensorServiceConnection connection;

    public MeasuringService(){
        super("MeasuringService");
    }
//==================================================================================================
    //location methods

    private void setLocation(){

        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        locationListener = new MeasuringLocationListener();
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
        } catch (SecurityException e){
            makeToastLong(context, "no permission for gps");
        }
    }
//==================================================================================================
    //lifecycle methods

    @Override
    public void onCreate(){
        super.onCreate();
        started = false;
        context = getApplicationContext();

        measurementArray = new MeasurementArray();
        connection = new SensorServiceConnection();

        Intent intent = new Intent(context, SensorService.class);
        bindService(intent, connection, Service.BIND_AUTO_CREATE);

        setLocation();
    }

    @Override
    public boolean onUnbind(Intent intent){

        unbindService(connection);
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy(){

        try{
            SharedPreferences preferences = getSharedPreferences("commeto", MODE_PRIVATE);
            int id = measurementArray.toJson(
                    context,
                    preferences.getString("username", "offline"),
                    preferences.getInt("calibration", 19),
                    type
            );
            Intent resultIntent = new Intent(context, RideDisplayActivity.class);
            resultIntent.putExtra("id", id);
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(resultIntent);
        } catch (NoDistanceException e){
            makeToastLong(context, e.getMessage());
        }catch (Exception e){
            makeToastLong(context, e.getMessage());
        } finally {
            deleteFromCache(context, "info");
            deleteFromCache(context, "backup");
        }

        locationManager.removeUpdates(locationListener);
        super.onDestroy();
    }
//==================================================================================================
    //public methods

    public void giveName(String name){
        this.measurementArray.setName(name);
    }
}
