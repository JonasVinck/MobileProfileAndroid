package com.commeto.kuleuven.commetov2.dataClasses;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import com.commeto.kuleuven.commetov2.exceptions.NoDistanceException;
import com.commeto.kuleuven.commetov2.sqlSupport.LocalDatabase;
import com.commeto.kuleuven.commetov2.sqlSupport.LocalRoute;
import com.commeto.kuleuven.commetov2.support.InternalIO;
import com.commeto.kuleuven.commetov2.support.Static;

import static com.commeto.kuleuven.commetov2.support.Static.makeToastLong;

/**
 * Created by Jonas on 1/03/2018.
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

    public String toString(){
        return
                this.name + "," +
                Long.toString(this.time) + "," +
                Double.toString(this.averageSpeed) + "," +
                Double.toString(this.distance);
    }

    public void add(Measurement measurement, boolean counts){

        if(counts) distance += measurement.getDistance();
        averageSpeed = (averageSpeed * list.size() + measurement.getSpeed()) / (list.size() + 1);
        list.addLast(measurement);
    }

    public int toJson(Context context, String username, int calibration, String type) throws NoDistanceException {

        if(distance != 0) {
            int id = Static.getIDInteger(context);

            DateFormat dateFormat = SimpleDateFormat.getDateInstance();
            LocalDatabase localDatabase = LocalDatabase.getInstance(context);
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
                route.put("id", id);
                JSONArray measurements = new JSONArray();
                for (Measurement measurement : list) {

                    measurements.put(measurement.toJSON());
                }
                route.put("measurements", measurements);
                InternalIO.writeToInternal(context, Integer.toString(id) + ".json", route.toString(), false);
            } catch (JSONException e) {
                makeToastLong(context, e.getMessage());
            }
            return id;
        } else throw new NoDistanceException("Rit heeft geen afstand.");
    }
}
