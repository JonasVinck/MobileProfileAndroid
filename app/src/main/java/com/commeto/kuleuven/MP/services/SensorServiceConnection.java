package com.commeto.kuleuven.MP.services;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.LinkedList;

/**
 * Created by Jonas on 26/03/2018.
 */

public class SensorServiceConnection implements ServiceConnection{

    private SensorService sensorService;
    private boolean bound;

    public SensorServiceConnection(){
        sensorService = null;
        bound = false;
    }

    public LinkedList<float[]> getAccelerometerData(){
        return sensorService.getAccelerometerData();
    }

    public LinkedList<float[]> getLightsensorData(){
        return sensorService.getLightSensorData();
    }

    public boolean isBound() {
        return bound;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

        SensorService.SensorBinder binder =
                (SensorService.SensorBinder) iBinder;
        sensorService = binder.getService();
        bound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

        sensorService = null;
        bound = false;
    }
}
