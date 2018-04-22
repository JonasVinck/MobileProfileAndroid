package com.commeto.kuleuven.MP.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;

import java.util.LinkedList;

/**
 * Created by Jonas on 1/03/2018.
 *
 * Service to get sensor data.
 */

public class SensorService extends IntentService implements SensorEventListener{

//==================================================================================================
    //sensor listener methods

    @Override
    public void onSensorChanged(SensorEvent sensorEvent){

        if(sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT){
            lightSensorData.addLast(sensorEvent.values.clone());
        }
        if(sensorEvent.sensor.getType() == Sensor.TYPE_GRAVITY){
            gravityVals = sensorEvent.values;
        }
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            float[] tempAccel = sensorEvent.values.clone();
            tempAccel[0] -= gravityVals[0];
            tempAccel[1] -= gravityVals[1];
            tempAccel[2] -= gravityVals[2];
            accelerometerData.addLast(tempAccel);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){
    }
//==================================================================================================
    //handler

    @Override
    public void onHandleIntent(Intent intent){
    }
//==================================================================================================
    //Binder class

    public class SensorBinder extends Binder {
        public SensorService getService() {
            return SensorService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

//==================================================================================================
    //class spec

    private final SensorBinder binder = new SensorBinder();

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor lightSensor;
    private Sensor gravity;
    private float[] gravityVals = {0,0,0};

    private LinkedList<float[]> accelerometerData;
    private LinkedList<float[]> lightSensorData;

    public SensorService(){
        super("SensorService");
    }
//==================================================================================================
    //lifecycle methods

    @Override
    public void onCreate(){
        super.onCreate();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = null;
        lightSensor = null;
        accelerometerData = new LinkedList<>();
        lightSensorData = new LinkedList<>();

        if(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        if(sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null){
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }
        if(sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null){
            gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        }

        sensorManager.registerListener(this, accelerometer, 10000);
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, gravity, 10000);
    }

    @Override
    public void onDestroy(){
        sensorManager.unregisterListener(this, accelerometer);
        sensorManager.unregisterListener(this, lightSensor);
        sensorManager.unregisterListener(this, gravity);
        super.onDestroy();
    }
//==================================================================================================
    //getters

    /**
     * Method to get acceleration data. When data has been retrieved, array is cleared.
     *
     * @return Acceleration data
     */

    public LinkedList<float[]> getAccelerometerData() {

        LinkedList<float[]> temp = new LinkedList<>(accelerometerData);
        accelerometerData = new LinkedList<>();
        return temp;
    }

    /**
     * Method to get light level data. When data has been retrieved, array is cleared.
     *
     * @return Light level data
     */

    public LinkedList<float[]> getLightSensorData() {

        LinkedList<float[]> temp = new LinkedList<>(lightSensorData);
        lightSensorData = new LinkedList<>();
        return temp;
    }
}
