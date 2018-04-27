package com.commeto.kuleuven.MP.sqlSupport;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

/**
 * <pre>
 * Created by Jonas on 2/03/2018.
 *
 * LocalRoute object to define rides and to persist rides to Room.
 * </pre>
 */

@Entity
public class LocalRoute {

    @PrimaryKey(autoGenerate = false)
    private int localId;

    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "sent")
    private boolean sent;

    @ColumnInfo(name = "username")
    private String username;

    @ColumnInfo(name = "ridename")
    private String ridename;

    @ColumnInfo(name = "speed")
    private double speed;

    @ColumnInfo(name = "distance")
    private double distance;

    @ColumnInfo(name = "time")
    private long time;

    @ColumnInfo(name = "duration")
    private long duration;

    @ColumnInfo(name = "calibration")
    private int calibration;

    @ColumnInfo(name = "type")
    private String type;

    @ColumnInfo(name = "updated")
    private boolean updated;

    @ColumnInfo(name = "lastUpdated")
    private long lastUpdated;

    @ColumnInfo(name = "description")
    private String description;

    public LocalRoute(int localId, int id, boolean sent, String username, String ridename, double speed, double distance, long time, long duration, int calibration, String type, boolean updated, long lastUpdated, String description) {
        this.localId = localId;
        this.id = id;
        this.sent = sent;
        this.username = username;
        this.ridename = ridename;
        this.speed = speed;
        this.distance = distance;
        this.time = time;
        this.duration = duration;
        this.calibration = calibration;
        this.type = type;
        this.updated = updated;
        this.lastUpdated = lastUpdated;
        this.description = description;
    }

    @Ignore
    public LocalRoute(){
        this.localId = 0;
        this.id = 0;
        this.sent = true;
        this.username = "none";
        this.ridename = "none";
        this.speed = 0;
        this.distance = 0;
        this.time = 0;
        this.duration = 0;
        this.calibration = 0;
        this.type = "none";
        this.updated = false;
        this.lastUpdated = 0;
        this.description = "none";
    }

    //=================================================================================================
    //getters

    public int getLocalId() {
        return localId;
    }

    public int getId() {
        return id;
    }

    public boolean isSent() {
        return sent;
    }

    public String getUsername() {
        return username;
    }

    public String getRidename() {
        return ridename;
    }

    public double getSpeed() {
        return speed;
    }

    public double getDistance() {
        return distance;
    }

    public long getTime() {
        return time;
    }

    public long getDuration() {
        return duration;
    }

    public int getCalibration() {
        return calibration;
    }

    public String getType() {
        return type;
    }

    public boolean isUpdated() {
        return updated;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public String getDescription() {
        return description;
    }
    //=================================================================================================
    //setters

    public void setLocalId(int localId) {
        this.localId = localId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRidename(String ridename) {
        this.ridename = ridename;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setCalibration(int calibration) {
        this.calibration = calibration;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setDescription(String description) {
        this.description = description;
    }
//==================================================================================================
    //debug

    @Override
    public String toString() {
        return "LocalRoute{" +
                "localId=" + localId +
                ", id=" + id +
                ", sent=" + sent +
                ", username='" + username + '\'' +
                ", ridename='" + ridename + '\'' +
                ", speed=" + speed +
                ", distance=" + distance +
                ", time=" + time +
                ", duration=" + duration +
                ", calibration=" + calibration +
                ", type='" + type + '\'' +
                ", updated=" + updated +
                ", lastUpdated=" + lastUpdated +
                ", description='" + description + '\'' +
                '}';
    }
}
