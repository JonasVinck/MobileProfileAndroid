package com.commeto.kuleuven.MP.services;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.LinkedList;

/**
 * Created by Jonas on 26/03/2018.
 *
 * <p>
 * Connection for the sensorService.
 * </p>
 */

public class SensorServiceConnection implements ServiceConnection{

    private SensorService sensorService;
    private boolean bound;

    public SensorServiceConnection(){
        sensorService = null;
        bound = false;
    }

    /**
     * Gets the accelerometer data from the SensorService.
     *
     * @return A list containing float arrays with 3 elements.
     */
    public LinkedList<float[]> getAccelerometerData(){
        return sensorService.getAccelerometerData();
    }

    /**
     * Gets the light level data from the SensorService.
     *
     * @return A list containing a float array with 1 element.
     */
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
