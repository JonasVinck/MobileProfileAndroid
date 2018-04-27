package com.commeto.kuleuven.MP.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;

import com.commeto.kuleuven.MP.activities.LoginActivity;
import com.commeto.kuleuven.MP.dataClasses.HTTPResponse;
import com.commeto.kuleuven.MP.http.GetTask;
import com.commeto.kuleuven.MP.http.PostTask;
import com.commeto.kuleuven.MP.interfaces.AsyncResponseInterface;
import com.commeto.kuleuven.MP.interfaces.SyncInterface;
import com.commeto.kuleuven.MP.R;
import com.commeto.kuleuven.MP.sqlSupport.LocalDatabase;
import com.commeto.kuleuven.MP.sqlSupport.LocalRoute;
import com.commeto.kuleuven.MP.sqlSupport.LocalRouteDAO;
import com.commeto.kuleuven.MP.support.InternalIO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static com.commeto.kuleuven.MP.http.HTTPStatic.getRouteJson;
import static com.commeto.kuleuven.MP.support.NotifyStatic.postNotification;
import static com.commeto.kuleuven.MP.support.Static.getIDInteger;

/**
 * Created by Jonas on 15/04/2018.
 *
 * <p>
 * Service to sync local database with server.
 * </p>
 *
 * <p>
 *     Service will send a request to get all rides on the server. The response will contain an
 *     array of json objects containing the id of the ride on the server and the time it was last
 *     modified.
 * </p>
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
    //constants

    //TODO Move to resources.
    private final String DELETED = "deleted";
    private final String LAST_CHANGE = "lastChange";
    private final String NAME = "name";
    private final String RIDE_ID = "rideId";
    private final String START_TIME = "startTime";
    private final String DISTANCE = "distance";
    private final String SPEED = "avSpeed";
    private final String DURATION = "duration";
    private final String TYPE = "type";
    private final String DESCRIPTION = "description";
    private final String SNAPPED_EXTENSION = "_snapped.json";
    private final String UNSNAPPED_EXTENSION = "_unsnapped.json";
    private final String SNAPPED = "jsonArraySnapped";
    private final String UNSNAPPED = "jsonArrayUnsnapped";
    private final String USERNAME = "username";
    private final String TOKEN = "token";
    private String FULL_IP;
//==================================================================================================
    //class specs

    private int toPull, toUpload, toUpdate;
    private int pulled, uploaded, updated;
    private SharedPreferences preferences;
    private String username;
    private String token;
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
        username = preferences.getString(USERNAME, "");
        token = preferences.getString(TOKEN, "");
        FULL_IP =
                preferences.getString(getString(R.string.preferences_ip), getString(R.string.hard_coded_ip)) +
                ":" +
                preferences.getString(getString(R.string.preferences_socket), getString(R.string.hard_coded_socket));
        dao = LocalDatabase.getInstance(getApplicationContext()).localRouteDAO();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        new GetTask(
                FULL_IP,
                getString(R.string.pull_all),
                getAllInterface,
                null,
                -1
        ).execute(
                preferences.getString(USERNAME, ""),
                preferences.getString(TOKEN, "")
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

    /**
     * <p>
     * Method to process the gotten JSONArray containing all ids.
     * </p>
     *
     * <p>
     *     If there is no ride in the Room database with the given server id, a get message will be
     *     sent to get the ride from the server. Otherwise the lastChanged time will be compared.
     *     Is the ride on the server more recent, a pull message will be sent, otherwise the local
     *     ride info will be pushed to the server.
     * </p>
     *
     * @param jsonArray JSONArray containing all ids and modified times of the rides on te server/
     */
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


                        if (localRoute.getLastUpdated() < jsonArray.getJSONObject(i).getLong(LAST_CHANGE)){

                            new GetTask(
                                    FULL_IP,
                                    getString(R.string.pull) + Integer.toString(localRoute.getId()) + "/0",
                                    updatePullInterface,
                                    null,
                                    localRoute.getId()
                            ).execute(
                                    preferences.getString(USERNAME, ""),
                                    preferences.getString(TOKEN, "")
                            );
                        } else if(localRoute.getLastUpdated() != jsonArray.getJSONObject(i).getLong(LAST_CHANGE)){

                            JSONObject toWrite = new JSONObject();
                            toWrite.put(DELETED, false);
                            toWrite.put(NAME, localRoute.getRidename());
                            toWrite.put(DESCRIPTION, localRoute.getDescription());

                            new PostTask(
                                    FULL_IP,
                                    getString(R.string.push) + Integer.toString(localRoute.getId()) + "/" + Long.toString(localRoute.getLastUpdated()),
                                    updatePushInterface,
                                    localRoute.getId(),
                                    null
                            ).execute(
                                    toWrite.toString(),
                                    preferences.getString(USERNAME, ""),
                                    preferences.getString(TOKEN, "")
                            );
                        } else {
                            toPull--;
                        }
                    } else{

                        new GetTask(
                                FULL_IP,
                                getString(R.string.pull) + jsonArray.getJSONObject(i).getInt("id") + "/0",
                                newPullInterface,
                                null,
                                jsonArray.getJSONObject(i).getInt("id")
                        ).execute(
                                preferences.getString(USERNAME, ""),
                                preferences.getString(TOKEN, "")
                        );
                    }
                } catch (JSONException e){
                    toPull--;
                }
            }
        }

        if(toPull <= 0) push();
    }

    /**
     * Method used to push all the rides in the local Room database that have been modified or have
     * not been sent.
     */
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
                            FULL_IP,
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
                    toWrite.put(DELETED, false);
                    toWrite.put(NAME, localRoute.getRidename());
                    toWrite.put(DESCRIPTION, localRoute.getDescription());

                    new PostTask(
                            FULL_IP,
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

    /**
     * displays a notification saying the syncing has finished. As well as a few statistics.
     */
    private void done(){

        if(toUpload <= 0 && toPull <= 0 && toUpdate <= 0){
            syncInterface.endSync();
            postNotification(
                    getApplicationContext(),
                    getString(R.string.sync_complete),
                    (pulled == 0 ? "" : Integer.toString(pulled) + (pulled == 1 ? " rit" : " ritten") + " succesvol opgehaald.\n") +
                            (uploaded == 0 ? "" : Integer.toString(uploaded) + (uploaded == 1 ? " rit" : " ritten") + " succesvol geupload.\n") +
                            (updated == 0 ? "" : Integer.toString(updated) + (updated == 1 ? " rit" : " ritten") + " ritten succesvol geupdate.")
            );
        }
    }

    /**
     * Method that shows a notification if the algorithm was interrupted, letting the user know
     * that the rides have not been synced with the database.
     */
    private void interrupt(){

        postNotification(getApplicationContext(), getString(R.string.error), getString(R.string.sync_eror));

        syncInterface.endSync();
    }

    /**
     * Method to set a ride as sent and not updated.
     *
     * @param localRoute The ride to be updated.
     */
    private void update(LocalRoute localRoute){

        localRoute.setSent(true);
        localRoute.setUpdated(false);
    }
//==================================================================================================
    //interface

    /**
     * Interface to handle the response for getting the list of all rides.
     */
    private AsyncResponseInterface getAllInterface = new AsyncResponseInterface() {
        @Override
        public void processFinished(HTTPResponse response) {
            if(response.getResponseCode() == 401){
                startActivity(new Intent(getApplicationContext(), LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                interrupt();
            } else if(response.getResponseCode() == 200){
                try {
                    getAll(new JSONArray(response.getResponseBody()));
                } catch (JSONException e){
                    interrupt();
                }
            } else {
                interrupt();
            }
        }
    };

    /**
     * Interface to handle the response of getting a ride from the server and updating the local
     * ride.
     */
    private AsyncResponseInterface updatePullInterface = new AsyncResponseInterface() {
        @Override
        public void processFinished(HTTPResponse response) {

            boolean deleted = false;
            if(response.getResponseCode() == 200 && response.getId() != -1 && response.getId() != -2) {

                try {
                    JSONObject responseObject = new JSONObject(response.getResponseBody());
                    LocalRoute localRoute = dao.existsServerId(response.getId(), username).get(0);

                    if(responseObject.getString("code").equals("nok")){
                        dao.delete(localRoute);
                    }
                    deleted = responseObject.getBoolean(DELETED);
                    if(!deleted) {
                        localRoute.setId(responseObject.getInt(RIDE_ID));
                        localRoute.setTime(responseObject.getLong(START_TIME));
                        localRoute.setDistance(responseObject.getDouble(DISTANCE));
                        localRoute.setSpeed(responseObject.getDouble(SPEED));
                        localRoute.setRidename(responseObject.getString(NAME));
                        localRoute.setDuration(responseObject.getLong(DURATION));
                        localRoute.setType(responseObject.getString(TYPE));
                        localRoute.setDescription(responseObject.getString(DESCRIPTION));
                        localRoute.setUpdated(false);
                        localRoute.setLastUpdated(responseObject.getLong(LAST_CHANGE));
                        localRoute.setSent(true);

                        dao.update(localRoute);

                        InternalIO.writeToInternal(
                                getApplicationContext(),
                                Integer.toString(localRoute.getLocalId()) + SNAPPED_EXTENSION,
                                responseObject.getString(SNAPPED),
                                false
                        );
                        InternalIO.writeToInternal(
                                getApplicationContext(),
                                Integer.toString(localRoute.getLocalId()) + UNSNAPPED_EXTENSION,
                                responseObject.getString(UNSNAPPED),
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

    /**
     * Interface to handle the response from getting a ride that was not previously stored in the
     * Room database.
     */
    private AsyncResponseInterface newPullInterface = new AsyncResponseInterface() {
        @Override
        public void processFinished(HTTPResponse response) {

            boolean deleted = false;
            if(response.getResponseCode() == 200 && response.getId() != -1 && response.getId() != -2) {
                try {
                    JSONObject responseObject = new JSONObject(response.getResponseBody());

                    deleted = responseObject.getBoolean(DELETED);
                    if(!deleted) {
                        LocalRoute localRoute = new LocalRoute(
                                getIDInteger(getApplicationContext()),
                                responseObject.getInt(RIDE_ID),
                                true,
                                username,
                                responseObject.getString(NAME),
                                responseObject.getDouble(SPEED),
                                responseObject.getDouble(DISTANCE),
                                responseObject.getLong(START_TIME),
                                responseObject.getLong(DURATION),
                                0,
                                responseObject.getString(TYPE),
                                false,
                                responseObject.getLong(LAST_CHANGE),
                                responseObject.getString(DESCRIPTION)
                        );
                        dao.insert(localRoute);

                        InternalIO.writeToInternal(
                                getApplicationContext(),
                                Integer.toString(localRoute.getLocalId()) + SNAPPED_EXTENSION,
                                responseObject.getString(SNAPPED),
                                false
                        );
                        InternalIO.writeToInternal(
                                getApplicationContext(),
                                Integer.toString(localRoute.getLocalId()) + UNSNAPPED_EXTENSION,
                                responseObject.getString(UNSNAPPED),
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

    /**
     * Interface to handle the response from pushing an update to the server.
     */
    private AsyncResponseInterface updatePushInterface = new AsyncResponseInterface() {
        @Override
        public void processFinished(HTTPResponse response) {
            if(response.getResponseCode() == 200 && response.getResponseCode() != -1 && response.getResponseCode() != -2){

                try {
                    if (new JSONObject(response.getResponseBody()).getString("code").equals("ok")) {
                        LocalRoute localRoute = dao.existsServerId(response.getId(), username).get(0);
                        update(localRoute);
                        dao.update(localRoute);
                    }
                } catch (JSONException e){
                    //catch possible JSONException.
                }
            } else if(response.getResponseCode() == 401) interrupt();

            updated++;
            toPull--;
            if(toPull <= 0) push();
        }
    };

    /**
     * Interface to handle the response from sending a ride not yet sent to the server.
     */
    private AsyncResponseInterface notSentPushInterface = new AsyncResponseInterface() {
        @Override
        public void processFinished(HTTPResponse response) {
            if(response.getResponseCode() == 200 && response.getResponseCode() != -1 && response.getResponseCode() != -2) {

                try {
                    if (new JSONObject(response.getResponseBody()).getString("code").equals("ok")) {
                        LocalRoute localRoute = dao.existsServerId(response.getId(), username).get(0);
                        localRoute.setId(response.getId());
                        update(localRoute);
                        dao.update(localRoute);
                    }
                } catch (JSONException e){
                    //Catch possible JSONException.
                }
            } else if(response.getResponseCode() == 401) interrupt();

            toUpload--;
            uploaded++;
            if (toUpload <= 0) done();
        }
    };

    /**
     * Interface to handle the response from pushing an updated ride to the server.
     */
    private AsyncResponseInterface updatedPushInterface = new AsyncResponseInterface() {
        @Override
        public void processFinished(HTTPResponse response) {

            if(response.getResponseCode() == 200 && response.getResponseCode() != -1 && response.getResponseCode() != -2) {

                try {
                    LocalRoute localRoute = dao.exists(response.getId(), username).get(0);
                    update(localRoute);
                    dao.update(localRoute);
                } catch (NullPointerException e){
                    //Ride doesn't exist...somehow...
                }
            } else if(response.getResponseCode() == 401) interrupt();

            toUpdate++;
            updated++;
            if(toUpdate <= 0) done();
        }
    };
//==================================================================================================

    /**
     * Set the interface to which the response will be sent when the service is complete.
     *
     * @param syncInterface Interface to be set.
     */
    public void setSyncInterface(SyncInterface syncInterface){
        this.syncInterface = syncInterface;
    }
}
