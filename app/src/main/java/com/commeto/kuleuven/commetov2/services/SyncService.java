package com.commeto.kuleuven.commetov2.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;

import com.commeto.kuleuven.commetov2.activities.LoginActivity;
import com.commeto.kuleuven.commetov2.dataClasses.HTTPResponse;
import com.commeto.kuleuven.commetov2.http.GetTask;
import com.commeto.kuleuven.commetov2.http.PostTask;
import com.commeto.kuleuven.commetov2.interfaces.AsyncResponseInterface;
import com.commeto.kuleuven.commetov2.interfaces.SyncInterface;
import com.commeto.kuleuven.commetov2.R;
import com.commeto.kuleuven.commetov2.sqlSupport.LocalDatabase;
import com.commeto.kuleuven.commetov2.sqlSupport.LocalRoute;
import com.commeto.kuleuven.commetov2.sqlSupport.LocalRouteDAO;
import com.commeto.kuleuven.commetov2.support.InternalIO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static com.commeto.kuleuven.commetov2.http.HTTPStatic.getRouteJson;
import static com.commeto.kuleuven.commetov2.support.NotifyStatic.postNotification;
import static com.commeto.kuleuven.commetov2.support.Static.getIDInteger;

/**
 * Created by Jonas on 15/04/2018.
 */

public class SyncService extends IntentService{

//==================================================================================================
    //hande intent

    public void onHandleIntent(Intent intent){
    }
//==================================================================================================
    //binder

    public class SyncServiceBinder extends Binder{
        public SyncService getService() {
            return SyncService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
//==================================================================================================
    //class specs

    private int toPull, toUpload, toUpdate;
    private int pulled, uploaded, updated;
    private SharedPreferences preferences;
    private String username;
    private String token;
    private String fullIp;
    private LocalRouteDAO dao;

    private SyncInterface syncInterface;

    private SyncServiceBinder binder = new SyncServiceBinder();

    public SyncService(){super("SyncService");}
//==================================================================================================
    //run methods

    @Override
    public void onCreate(){
        toPull = 0;
        toUpload = 0;
        toUpdate = 0;

        preferences = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        username = preferences.getString("username", "");
        token = preferences.getString("token", "");
        fullIp =
                preferences.getString("baseUrl", getString(R.string.hard_coded_ip)) +
                ":" +
                preferences.getString("socket", getString(R.string.hard_coded_socket));
        dao = LocalDatabase.getInstance(getApplicationContext()).localRouteDAO();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        new GetTask(
                fullIp,
                "/MP/service/secured/ride/pullall/0",
                getAllInterface,
                null,
                -1
        ).execute(
                preferences.getString("username", ""),
                preferences.getString("token", "")
        );

        return START_NOT_STICKY;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onDestroy(){
    }
//==================================================================================================
    //private functions

    private void getAll(JSONArray jsonArray){
        toPull = jsonArray.length();

        if(toPull <= 0) push();
        else {

            List<LocalRoute> localRoutes;
            LocalRoute localRoute;

            for (int i = 0; i < jsonArray.length(); i++) {

                try {
                     localRoutes = dao.existsServerId(jsonArray.getJSONObject(i).getInt("id"), username);
                     if (!localRoutes.isEmpty()) {
                        localRoute = localRoutes.get(0);


                        if (localRoute.getLastUpdated() < jsonArray.getJSONObject(i).getLong("lastChange")){

                            new GetTask(
                                    fullIp,
                                    getString(R.string.pull) + Integer.toString(localRoute.getId()) + "/0",
                                    updatePullInterface,
                                    null,
                                    localRoute.getId()
                            ).execute(
                                    preferences.getString("username", ""),
                                    preferences.getString("token", "")
                            );
                        } else if(localRoute.getLastUpdated() != jsonArray.getJSONObject(i).getLong("lastChange")){

                            JSONObject toWrite = new JSONObject();
                            toWrite.put("deleted", false);
                            toWrite.put("name", localRoute.getRidename());
                            toWrite.put("description", localRoute.getDescription());

                            new PostTask(
                                    fullIp,
                                    getString(R.string.push) + Integer.toString(localRoute.getId()) + "/" + Long.toString(localRoute.getLastUpdated()),
                                    updatePushInterface,
                                    localRoute.getId(),
                                    null
                            ).execute(
                                    toWrite.toString(),
                                    preferences.getString("username", ""),
                                    preferences.getString("token", "")
                            );
                        } else {
                            toPull--;
                        }
                    } else{

                        new GetTask(
                                fullIp,
                                getString(R.string.pull) + jsonArray.getJSONObject(i).getInt("id") + "/0",
                                newPullInterface,
                                null,
                                jsonArray.getJSONObject(i).getInt("id")
                        ).execute(
                                preferences.getString("username", ""),
                                preferences.getString("token", "")
                        );
                    }
                } catch (JSONException e){
                    toPull--;
                }
            }
        }

        if(toPull <= 0) push();
    }

    private void push(){

        List<LocalRoute> allNotSent = dao.getAllNotSent(username);
        toUpload = allNotSent.size();
        List<LocalRoute> allUpdated = dao.getAllUpdated(username);
        toUpdate = allUpdated.size();

        LocalRoute localRoute;

        if(toUpload == 0 && toUpdate == 0) done();
        else {
            for(int i = 0; i < allNotSent.size(); i++){

                try {
                    localRoute = allNotSent.get(i);
                    new PostTask(
                            fullIp,
                            getString(R.string.push) + Integer.toString(localRoute.getId()) + "/" + Long.toString(localRoute.getLastUpdated()),
                            notSentPushInterface,
                            localRoute.getLocalId(),
                            null
                    ).execute(
                            getRouteJson(getApplicationContext(), localRoute),
                            username,
                            token
                    );
                } catch (Exception e){
                    toUpload--;
                }
            }
            for(int i = 0; i < allUpdated.size(); i++){

                try {

                    localRoute = allUpdated.get(i);
                    JSONObject toWrite = new JSONObject();
                    toWrite.put("deleted", false);
                    toWrite.put("name", localRoute.getRidename());
                    toWrite.put("description", localRoute.getDescription());

                    new PostTask(
                            fullIp,
                            getString(R.string.push) + Integer.toString(localRoute.getId()) + "/" + Long.toString(localRoute.getLastUpdated()),
                            updatedPushInterface,
                            allUpdated.get(i).getLocalId(),
                            null
                    ).execute(
                            toWrite.toString(),
                            username,
                            token
                    );
                } catch (Exception e){
                    toUpload--;
                }
            }
        }

        if(toUpload <= 0 && toUpdate <= 0) done();
    }

    private void done(){

        if(toUpload <= 0 && toPull <= 0 && toUpdate <= 0){
            syncInterface.endSync();
            if(updated != 0 || uploaded != 0 || pulled != 0) {
                postNotification(
                        getApplicationContext(),
                        getString(R.string.sync_complete),
                        (pulled == 0 ? "" : Integer.toString(pulled) + (pulled == 1 ? " rit" : " ritten") + " succesvol opgehaald.\n") +
                                (uploaded == 0 ? "" : Integer.toString(uploaded) + (uploaded == 1 ? " rit" : " ritten") + " succesvol geupload.\n") +
                                (updated == 0 ? "" : Integer.toString(updated) + (updated == 1 ? " rit" : " ritten") + " ritten succesvol geupdate.")
                );
            } else {
                postNotification(getApplicationContext(), "done", "");
            }
        }
    }

    private void interrupt(){

        postNotification(getApplicationContext(), getString(R.string.error), getString(R.string.sync_eror));

        syncInterface.endSync();
    }

    private void update(int id){


        LocalRoute localRoute = dao.exists(id, username).get(0);
        localRoute.setSent(true);
        localRoute.setUpdated(false);
        dao.update(localRoute);
    }
//==================================================================================================
    //interface

    private AsyncResponseInterface getAllInterface = new AsyncResponseInterface() {
        @Override
        public void processFinished(HTTPResponse response) {
            if(response.getResponseCode() == 401){
                startActivity(new Intent(getApplicationContext(), LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                interrupt();
            } else if(response.getResponseCode() == 200){
                try {
                    getAll(new JSONArray(response.getResponsBody()));
                } catch (JSONException e){
                    interrupt();
                }
            } else {
                interrupt();
            }
        }
    };

    private AsyncResponseInterface updatePullInterface = new AsyncResponseInterface() {
        @Override
        public void processFinished(HTTPResponse response) {

            boolean deleted = false;
            if(response.getResponseCode() == 200 && response.getId() != -1 && response.getId() != -2) {

                try {
                    JSONObject responseObject = new JSONObject(response.getResponsBody());
                    LocalRoute localRoute = dao.existsServerId(response.getId(), username).get(0);

                    deleted = responseObject.getBoolean("deleted");
                    if(!deleted) {
                        localRoute.setId(responseObject.getInt("rideId"));
                        localRoute.setTime(responseObject.getLong("startTime"));
                        localRoute.setDistance(responseObject.getDouble("distance"));
                        localRoute.setSpeed(responseObject.getDouble("avSpeed"));
                        localRoute.setRidename(responseObject.getString("name"));
                        localRoute.setDuration(responseObject.getLong("duration"));
                        localRoute.setType(responseObject.getString("type"));
                        localRoute.setDescription(responseObject.getString("description"));
                        localRoute.setUpdated(false);
                        localRoute.setLastUpdated(responseObject.getLong("lastChange"));
                        localRoute.setSent(true);

                        dao.update(localRoute);

                        InternalIO.writeToInternal(
                                getApplicationContext(),
                                Integer.toString(localRoute.getLocalId()) + "_snapped.json",
                                responseObject.getString("jsonArraySnapped"),
                                false
                        );
                        InternalIO.writeToInternal(
                                getApplicationContext(),
                                Integer.toString(localRoute.getLocalId()) + "_unsnapped.json",
                                responseObject.getString("jsonArrayUnsnapped"),
                                false
                        );
                    } else {
                        dao.delete(localRoute);
                    }
                } catch (JSONException e){}
            } else if(response.getResponseCode() == 401) interrupt();

            if(!deleted) pulled++;
            toPull--;
            if(toPull <= 0) push();
        }
    };

    private AsyncResponseInterface newPullInterface = new AsyncResponseInterface() {
        @Override
        public void processFinished(HTTPResponse response) {

            boolean deleted = false;
            if(response.getResponseCode() == 200 && response.getId() != -1 && response.getId() != -2) {
                try {
                    JSONObject responseObject = new JSONObject(response.getResponsBody());

                    deleted = responseObject.getBoolean("deleted");
                    if(!deleted) {
                        LocalRoute localRoute = new LocalRoute(
                                getIDInteger(getApplicationContext()),
                                responseObject.getInt("rideId"),
                                true,
                                username,
                                responseObject.getString("name"),
                                responseObject.getDouble("avSpeed"),
                                responseObject.getDouble("distance"),
                                responseObject.getLong("startTime"),
                                responseObject.getLong("duration"),
                                0,
                                responseObject.getString("type"),
                                false,
                                responseObject.getLong("lastChange"),
                                responseObject.getString("description")
                        );
                        dao.insert(localRoute);

                        InternalIO.writeToInternal(
                                getApplicationContext(),
                                Integer.toString(localRoute.getLocalId()) + "_snapped.json",
                                responseObject.getString("jsonArraySnapped"),
                                false
                        );
                        InternalIO.writeToInternal(
                                getApplicationContext(),
                                Integer.toString(localRoute.getLocalId()) + "_unsnapped.json",
                                responseObject.getString("jsonArrayUnsnapped"),
                                false
                        );
                    }
                } catch (JSONException e){}
            } else if(response.getResponseCode() == 401) interrupt();

            toPull--;
            if(!deleted) pulled++;
            if(toPull <= 0) push();
        }
    };

    private AsyncResponseInterface updatePushInterface = new AsyncResponseInterface() {
        @Override
        public void processFinished(HTTPResponse response) {
            if(response.getResponseCode() == 200 && response.getId() != -1 && response.getId() != -2){

                update(response.getId());
            } else if(response.getResponseCode() == 401) interrupt();

            updated++;
            toPull--;
            if(toPull <= 0) push();
        }
    };

    private AsyncResponseInterface notSentPushInterface = new AsyncResponseInterface() {
        @Override
        public void processFinished(HTTPResponse response) {
            if(response.getResponseCode() == 200 && response.getId() != -1 && response.getId() != -2) {

                update(response.getId());
            } else if(response.getResponseCode() == 401) interrupt();

            toUpload--;
            uploaded++;
            if (toUpload <= 0) done();
        }
    };

    private AsyncResponseInterface updatedPushInterface = new AsyncResponseInterface() {
        @Override
        public void processFinished(HTTPResponse response) {

            if(response.getResponseCode() == 200 && response.getId() != -1 && response.getId() != -2) {

            } else if(response.getResponseCode() == 401) interrupt();

            toUpdate++;
            updated++;
            if(toUpdate <= 0) done();
        }
    };
//==================================================================================================

    public void setSyncInterface(SyncInterface syncInterface){
        this.syncInterface = syncInterface;
    }
}
