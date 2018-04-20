package com.commeto.kuleuven.commetov2.Services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;

import com.commeto.kuleuven.commetov2.Activities.LoginActivity;
import com.commeto.kuleuven.commetov2.DataClasses.HTTPResponse;
import com.commeto.kuleuven.commetov2.HTTP.GetTask;
import com.commeto.kuleuven.commetov2.HTTP.PostTask;
import com.commeto.kuleuven.commetov2.Interfaces.AsyncResponseInterface;
import com.commeto.kuleuven.commetov2.Interfaces.SyncInterface;
import com.commeto.kuleuven.commetov2.R;
import com.commeto.kuleuven.commetov2.SQLSupport.LocalDatabase;
import com.commeto.kuleuven.commetov2.SQLSupport.LocalRoute;
import com.commeto.kuleuven.commetov2.SQLSupport.LocalRouteDAO;
import com.commeto.kuleuven.commetov2.Support.InternalIO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static com.commeto.kuleuven.commetov2.HTTP.HTTPStatic.getRouteJson;
import static com.commeto.kuleuven.commetov2.Support.NotifyStatic.postNotification;
import static com.commeto.kuleuven.commetov2.Support.Static.getIDInteger;

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
    private long lastChanged;
    private long currentCahnge;
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

        preferences = getSharedPreferences("commeto", MODE_PRIVATE);
        username = preferences.getString("username", "");
        token = preferences.getString("token", "");
        fullIp =
                preferences.getString("baseUrl", "213..118.13.252") +
                        ":" +
                        preferences.getString("socket", "443");
        lastChanged = preferences.getLong("last_changed", 0);
        currentCahnge = System.currentTimeMillis();
        preferences.edit().putLong("last_changed", System.currentTimeMillis()).apply();
        dao = LocalDatabase.getInstance(getApplicationContext()).localRouteDAO();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        new GetTask(
                fullIp,
                "/MP/service/secured/ride/pullall/" + Long.toString(lastChanged),
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
            for (int i = 0; i < jsonArray.length(); i++) {

                try {
                    new GetTask(
                            fullIp,
                            "/MP/service/secured/ride/pull/" + Integer.toString(jsonArray.getJSONObject(i).getInt("id")) + "/0",
                            getRideInterface,
                            null,
                            jsonArray.getJSONObject(i).getInt("id")
                    ).execute(
                            username,
                            token
                    );
                } catch (JSONException e) {
                    toPull--;
                }
            }
        }
    }

    private void push(){
        List<LocalRoute> localRoutesNotSent = dao.getAllNotSent(username);
        toUpload = localRoutesNotSent.size();
        for(LocalRoute localRoute: localRoutesNotSent){

            try {
                new PostTask(
                        fullIp,
                        "/MP/service/secured/measurement",
                        uploadInterface,
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

        List<LocalRoute> localRoutesUpdated = dao.getAllUpdated(username);
        toUpdate = localRoutesUpdated.size();
        for(LocalRoute localRoute: localRoutesUpdated){

            try {
                JSONObject toWrite = new JSONObject();
                toWrite.put("deleted", false)
                        .put("name", localRoute.getRidename())
                        .put("description", localRoute.getDescription());

                new PostTask(
                        fullIp,
                        "/MP/service/secured/ride/push/" + Integer.toString(localRoute.getId()) + "/" + Long.toString(localRoute.getLastUpdated()),
                        updateInterface,
                        localRoute.getLocalId(),
                        null
                ).execute(
                        toWrite.toString(),
                        username,
                        token
                );
            } catch (Exception e){
                InternalIO.writeToLog(getApplicationContext(), e);
                toUpdate--;
            }
        }

        done();
    }

    private void done(){

        if(toUpload <= 0 && toPull <= 0 && toUpdate <= 0){
            syncInterface.endSync();
            preferences.edit().putLong("last_changed", currentCahnge).apply();
            if(updated != 0 || uploaded != 0 || pulled != 0) {
                postNotification(
                        getApplicationContext(),
                        getString(R.string.sync_complete),
                        (pulled == 0 ? "" : Integer.toString(pulled) + " ritten succesvol opgehaald.\n") +
                                (uploaded == 0 ? "" : Integer.toString(uploaded) + " ritten succesvol geupload.\n") +
                                (updated == 0 ? "" : Integer.toString(updated) + " ritten succesvol geupdate.")
                );
            }
        }
    }

    private void interrupt(){

        postNotification(getApplicationContext(), getString(R.string.error), getString(R.string.sync_eror));

        syncInterface.endSync();
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

    private AsyncResponseInterface getRideInterface = new AsyncResponseInterface() {
        @Override
        public void processFinished(HTTPResponse response) {
            toPull--;

            if(response.getResponseCode() == 200){
                try{
                    JSONObject responseObject = new JSONObject(response.getResponsBody());
                    if(responseObject.getString("code").equals("ok")){

                        LocalRoute localRoute = null;
                        List<LocalRoute> localRoutes = dao.existsServerId(response.getId(), username);
                        if (localRoutes.size() > 0) localRoute = localRoutes.get(0);

                        if(responseObject.getBoolean("deleted")){

                            if(localRoute != null) dao.delete(localRoute);
                        } else {
                            if(localRoute != null) {

                                if(localRoute.getLastUpdated() < responseObject.getLong("lastChange")) {
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

                                    pulled++;
                                } else if(localRoute.getLastUpdated() != responseObject.getLong("lastChange")){
                                    localRoute.setUpdated(true);
                                }
                                dao.update(localRoute);
                            } else {
                                localRoute = new LocalRoute(
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

                                dao.insert(localRoute);
                                pulled++;
                            }
                        }
                    }
                } catch (JSONException e){
                    InternalIO.writeToLog(getApplicationContext(), e);
                }
            } else if(response.getResponseCode() == 401) interrupt();

            if(toPull <= 0) push();
        }
    };

    private AsyncResponseInterface uploadInterface = new AsyncResponseInterface() {
        @Override
        public void processFinished(HTTPResponse response) {
            toUpload--;
            if(response.getResponseCode() == 200){

                String[] responseArray = response.getResponsBody().split(",");
                int localId = Integer.parseInt(responseArray[0]);
                if(localId != -1){

                    LocalRoute localRoute = dao.exists(localId, username).get(0);

                    if(localRoute != null){

                        localRoute.setSent(true);
                        localRoute.setId(Integer.parseInt(responseArray[1]));
                        localRoute.setUpdated(false);
                        dao.update(localRoute);
                    }
                }
            } else if (response.getResponseCode() == 401) interrupt();

            uploaded++;
            if(toUpload <= 0) done();
        }
    };

    private AsyncResponseInterface updateInterface = new AsyncResponseInterface() {
        @Override
        public void processFinished(HTTPResponse response) {
            toUpdate--;
            if(response.getResponseCode() == 200){

                try {
                    String[] responseArray = response.getResponsBody().split(",");
                    int localId = Integer.parseInt(responseArray[0]);
                    JSONObject object = new JSONObject(responseArray[1]);
                    if (localId != -1 && object.getString("code").equals("ok")) {

                        LocalRoute localRoute = dao.exists(localId, username).get(0);

                        if (localRoute != null) {

                            localRoute.setSent(true);
                            localRoute.setUpdated(false);
                            dao.update(localRoute);
                        }
                    }
                } catch (Exception e){
                    InternalIO.writeToLog(getApplicationContext(), e);
                }
            } else if (response.getResponseCode() == 401) interrupt();

            updated++;
            if(toUpdate <= 0) done();
        }
    };
//==================================================================================================

    public void setSyncInterface(SyncInterface syncInterface){
        this.syncInterface = syncInterface;
    }
}
