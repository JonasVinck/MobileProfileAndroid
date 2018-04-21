package com.commeto.kuleuven.commetov2.dataClasses;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

/**
 * Created by Jonas on 1/03/2018.
 */

public class Measurement{

//==================================================================================================
    //class specs

    private long time;
    private double latitude;
    private double longitude;
    private double elevation;
    private double accellerationResult;
    private LinkedList<float[]> accelerometerData;
    private double lightResult;
    private LinkedList<float[]> lightlevelData;
    private double speed;
    private double distance;
    private float accuracy;

    public Measurement(
            double latitude,
            double longitude,
            double elevation,
            LinkedList<float[]> accelerometerData,
            LinkedList<float[]> lightsensorData,
            double speed,
            double distance,
            float accuracy){

        this.time = System.currentTimeMillis();
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        if (accelerometerData == null || speed * 3.6 < 10 || speed * 3.6 > 25) this.accelerometerData = null;
        else this.accelerometerData = new LinkedList<>(accelerometerData);
        this.accellerationResult = calculateAccelerationResult();
        if (lightsensorData == null) this.lightlevelData = null;
        else this.lightlevelData = new LinkedList<>(lightsensorData);
        this.lightResult = calculateLightResult();
        this.speed = speed;
        this.distance = distance;
        this.accuracy = accuracy;
    }

    public Measurement(String[] backup){

        this.time = Long.parseLong(backup[0]);
        this.latitude = Double.parseDouble(backup[1]);
        this.longitude = Double.parseDouble(backup[2]);
        this.elevation = Double.parseDouble(backup[3]);
        this.accellerationResult = Double.parseDouble(backup[4]);
        this.accelerometerData = null;
        this.lightResult = Double.parseDouble(backup[5]);
        this.lightlevelData = null;
        this.speed = Double.parseDouble(backup[6]);
        this.distance = Double.parseDouble(backup[7]);
        this.accuracy = Float.parseFloat(backup[8]);
    }
//==================================================================================================
    //getters

    public long getTime() {
        return time;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getElevation() {
        return elevation;
    }

    public double getAccellerationResult() {
        return accellerationResult;
    }

    public double getLightResult() {
        return lightResult;
    }

    public double getSpeed() {
        return speed;
    }

    public double getDistance() {
        return distance;
    }
//==================================================================================================
    //private methods

    private double calculateAccelerationResult(){
        if(accelerometerData == null) return -1;
        else {
            try {
                double result = 0, tempResult = 0;
                int n = accelerometerData.size();
                double max = 0;
                for (float[] data : accelerometerData) {
                    tempResult = Math.sqrt(
                            Math.pow(data[0], 2) +
                                    Math.pow(data[1], 2) +
                                    Math.pow(data[2], 2)
                    );
                    if(tempResult > max) max = tempResult;
                    result += tempResult;
                }
                result = result / n;
                return result * 0.4 + max * 0.6;
            } catch (Exception e) {
            }
        }
        return 0;
    }

    private double calculateLightResult(){

        try {
            if (!lightlevelData.isEmpty()) {
                double result = 0;
                for (float[] data : lightlevelData) {
                    result += data[0];
                }
                result = result / lightlevelData.size();
                return result;
            }
        } catch (NullPointerException e){
        }
        return -1;
    }
//==================================================================================================
    //setters

    public void setTime(long time) {
        this.time = time;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    public void setAccellerationResult(double accellerationResult) {
        this.accellerationResult = accellerationResult;
    }

    public void setAccelerometerData(LinkedList<float[]> accelerometerData) {
        this.accelerometerData = accelerometerData;
    }

    public void setLightResult(double lightResult) {
        this.lightResult = lightResult;
    }

    public void setLightlevelData(LinkedList<float[]> lightlevelData) {
        this.lightlevelData = lightlevelData;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
//==================================================================================================
    //public methods

    public JSONObject toJSON() {
        JSONObject object = new JSONObject();

        try {

            object.put("time", time);
            object.put("lat", latitude);
            object.put("lon", longitude);
            object.put("ele", elevation);
            object.put("speed", speed);
            object.put("result", accellerationResult);
            object.put("lightResult", lightResult);
            object.put("accuracy", accuracy);

            JSONArray array = new JSONArray();
            JSONObject innerObject;

            if(accelerometerData != null) {
                for (float[] acceleration : accelerometerData) {

                    innerObject = new JSONObject();
                    innerObject.put("x", acceleration[0]);
                    innerObject.put("y", acceleration[1]);
                    innerObject.put("z", acceleration[2]);
                    array.put(innerObject);
                }
                object.put("measurements", array);
            }

            if(lightlevelData != null) {
                array = new JSONArray();
                for (float[] lightlevel : lightlevelData) {

                    innerObject = new JSONObject();
                    innerObject.put("value", lightlevel[0]);
                    array.put(innerObject);
                }
                object.put("lightlevelData", array);
            }
        } catch (JSONException e) {
        }
        return object;
    }

    public String toString(){
        return
                Long.toString(time) + "," +
                Double.toString(latitude) + "," +
                Double.toString(longitude) + "," +
                Double.toString(elevation) + "," +
                Double.toString(accellerationResult) + "," +
                Double.toString(lightResult) + "," +
                Double.toString(speed) + "," +
                Double.toString(distance) + "," +
                Float.toString(accuracy);
    }
}
