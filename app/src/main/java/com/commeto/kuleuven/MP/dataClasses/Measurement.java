package com.commeto.kuleuven.MP.dataClasses;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

/**
 * <pre>
 * Created by Jonas on 1/03/2018.
 *
 * Class meant to bundle the different measurement values and calculate the result.
 *
 * values:
 *  - Time of the measurement.
 *  - Latitude of the measurement.
 *  - longitude of the measurement.
 *  - Altitude (elevation) of the measurement.
 *  - The result calculated from the accelerometer data.
 *  - The accelerometer data.
 *  - The result calculated from the light level data.
 *  - The light level data.
 *  - The speed.
 *  - The covered distance.
 *  - The gps' accuracy.
 * </pre>
 */

public class Measurement{

//==================================================================================================
    //class specs

    private long time;
    private double latitude;
    private double longitude;
    private double elevation;
    private double accelerationResult;
    private LinkedList<float[]> accelerometerData;
    private double lightResult;
    private LinkedList<float[]> lightLevelData;
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
        this.accelerationResult = calculateAccelerationResult();
        if (lightsensorData == null) this.lightLevelData = null;
        else this.lightLevelData = new LinkedList<>(lightsensorData);
        this.lightResult = calculateLightResult();
        this.speed = speed;
        this.distance = distance;
        this.accuracy = accuracy;
    }

    /**
     * Constructor to reconstruct the measurement from a csv String.
     *
     * @param backup A CSV String.
     */
    public Measurement(String[] backup){

        this.time = Long.parseLong(backup[0]);
        this.latitude = Double.parseDouble(backup[1]);
        this.longitude = Double.parseDouble(backup[2]);
        this.elevation = Double.parseDouble(backup[3]);
        this.accelerationResult = Double.parseDouble(backup[4]);
        this.accelerometerData = null;
        this.lightResult = Double.parseDouble(backup[5]);
        this.lightLevelData = null;
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

    public double getAccelerationResult() {
        return accelerationResult;
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

    /**
     * <pre>
     * Reduces the accelerometerData array to 1 value.
     *
     * The result is 0.4 times the average of the amplitude values in the array added to 0.6 times
     * the maximum value in the array.
     *
     * Returns -1 if array is empty.
     * </pre>
     * @return The acceleration value of the measurement.
     */

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
                //EMpty, safety to prevent crash on empty array.
            }
        }
        return 0;
    }

    /**
     * <pre>
     * Reduces the light level array to 1 value.
     *
     * The result is the average of the values in the array.
     *
     * Returns -1 if array is empty.
     * </pre>
     * @return The light value for the measurement.
     */

    private double calculateLightResult(){

        try {
            if (!lightLevelData.isEmpty()) {
                double result = 0;
                for (float[] data : lightLevelData) {
                    result += data[0];
                }
                result = result / lightLevelData.size();
                return result;
            }
        } catch (NullPointerException e){
            //Catch if the array is a null pointer reference.
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

    public void setAccelerationResult(double accelerationResult) {
        this.accelerationResult = accelerationResult;
    }

    public void setAccelerometerData(LinkedList<float[]> accelerometerData) {
        this.accelerometerData = accelerometerData;
    }

    public void setLightResult(double lightResult) {
        this.lightResult = lightResult;
    }

    public void setLightLevelData(LinkedList<float[]> lightLevelData) {
        this.lightLevelData = lightLevelData;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
//==================================================================================================
    //public methods

    /**
     * Method used to get a JSON representation of the measurement.
     *
     * @return JSONObject form of the measurement.
     */

    public JSONObject toJSON() {
        JSONObject object = new JSONObject();

        try {
            object.put("time", time);
            object.put("lat", latitude);
            object.put("lon", longitude);
            object.put("ele", elevation);
            object.put("speed", speed);
            object.put("result", accelerationResult);
            object.put("lightResult", lightResult);
            object.put("accuracy", accuracy);
        } catch (JSONException e) {
            //Empty, to prevent crash on JSONException.
        }
        return object;
    }

    /**
     * Method to get a CSV form of the measurement to use to backup the measurements.
     *
     * @return The CSV form of the measurement.
     */
    public String toString(){
        return
                Long.toString(time) + "," +
                Double.toString(latitude) + "," +
                Double.toString(longitude) + "," +
                Double.toString(elevation) + "," +
                Double.toString(accelerationResult) + "," +
                Double.toString(lightResult) + "," +
                Double.toString(speed) + "," +
                Double.toString(distance) + "," +
                Float.toString(accuracy);
    }
}
