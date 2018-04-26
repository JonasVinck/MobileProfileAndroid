package com.commeto.kuleuven.MP.dataClasses;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import com.commeto.kuleuven.MP.exceptions.NoDistanceException;
import com.commeto.kuleuven.MP.sqlSupport.LocalDatabase;
import com.commeto.kuleuven.MP.sqlSupport.LocalRoute;
import com.commeto.kuleuven.MP.support.InternalIO;
import com.commeto.kuleuven.MP.support.Static;

import static com.commeto.kuleuven.MP.support.Static.makeToastLong;

/**
 * <pre>
 * Created by Jonas on 1/03/2018.
 *
 * Class used to bundle all Measurements together with some additional ride info:
 *  - The name of the ride.
 *  - The start time of the ride.
 *  - The average speed.
 *  - The total distance.
 * </pre>
 */

public class MeasurementArray {

//==================================================================================================
    //class specs

    private String name;
    private LinkedList<Measurement> list;
    private long time;
    private double averageSpeed;
    private double distance;

    public MeasurementArray(){
        name = null;
        list = new LinkedList<>();
        time = System.currentTimeMillis();
        averageSpeed = 0;
        distance = 0;
    }

    /**
     * <p>
     * Method to reconstruct the object from 2 CSV Strings. One containing the general info, like
     * the time and the name and one containing the array.
     * </p>
     *
     * @param info      CSV String representing the rides info.
     * @param backup    CSV String representing the measurement array.
     */
    public MeasurementArray(String[] info, String backup){
        this.name = info[0];
        this.time = Long.parseLong(info[1]);
        this.averageSpeed = Double.parseDouble(info[2]);
        this.distance = Double.parseDouble(info[3]);

        this.list = new LinkedList<>();
        String[] array = backup.split("\n");
        for(String string: array){
            list.add(new Measurement(string.split(",")));
        }
    }
//==================================================================================================
    //getters

    public LinkedList<Measurement> getList() {
        return list;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public double getDistance() {
        return distance;
    }

    public long getTime() {
        return time;
    }
//==================================================================================================
    //setters

    public void setName(String name) {
        this.name = name;
    }

//==================================================================================================
    //private methods

    private long getDuration(){
        try {
            return list.getLast().getTime() - list.getFirst().getTime();
        } catch (NoSuchElementException e){
            return 0;
        }
    }
//==================================================================================================
    //public methods

    /**
     * Method to generate CSV representation of the information of the object for future
     * reconstruction.
     *
     * @return CSV String representing the information.
     */

    public String toString(){
        return
                this.name + "," +
                Long.toString(this.time) + "," +
                Double.toString(this.averageSpeed) + "," +
                Double.toString(this.distance);
    }

    /**
     * Method ued to add a measurement to the array, only if the measurement counts.
     *
     * @param measurement Measurement to add.
     * @param counts boolean representing if the measurement counts towards the total distance.
     */

    public void add(Measurement measurement, boolean counts){

        if(counts) distance += measurement.getDistance();
        averageSpeed = (averageSpeed * list.size() + measurement.getSpeed()) / (list.size() + 1);
        list.addLast(measurement);
    }

    /**
     * Method to generate JSON representation of the MeasurementArray, writing it to storage and
     * adding the info to the room database. Throw NoDistanceException if ride has no distance.
     * Rides with no distance don't have to be saved.
     *
     * @param context Context of the application to use IO.
     * @param username Username of the rider.
     * @param calibration Used calibration.
     * @param type Type of the ride.
     * @return Id generated for the route
     * @throws NoDistanceException Thrown when ride has a distance of 0.
     */
    public int toJson(Context context, String username, int calibration, String type) throws NoDistanceException {

        if(distance != 0) {
            int id = Static.getIDInteger(context);

            DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
            LocalDatabase localDatabase = LocalDatabase.getInstance(context);
            //Default: use date as ride name.
            if (name == null || name.equals("")) name = dateFormat.format(new Date(time));
            localDatabase.localRouteDAO().insert(new LocalRoute(
                    id,
                    id,
                    false,
                    username,
                    name,
                    averageSpeed,
                    distance,
                    time,
                    getDuration(),
                    calibration,
                    type.toLowerCase(),
                    false,
                    System.currentTimeMillis(),
                    ""
            ));

            JSONObject route = new JSONObject();

            try {
                //Add ride id to json file for debugging purposes.
                route.put("id", id);
                JSONArray measurements = new JSONArray();
                for (Measurement measurement : list) {
                    //Add json representation of each measurement.
                    measurements.put(measurement.toJSON());
                }
                route.put("measurements", measurements);

                //Write to internal storage as {id}.json for easy retrieval.
                InternalIO.writeToInternal(context, Integer.toString(id) + ".json", route.toString(), false);
            } catch (JSONException e) {
                makeToastLong(context, e.getMessage());
            }
            return id;
        } else throw new NoDistanceException("Rit heeft geen afstand.");
    }
}
