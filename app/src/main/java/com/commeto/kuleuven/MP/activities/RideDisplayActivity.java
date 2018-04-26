package com.commeto.kuleuven.MP.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.commeto.kuleuven.MP.dataClasses.HTTPResponse;
import com.commeto.kuleuven.MP.dialogs.DescriptionDialog;
import com.commeto.kuleuven.MP.dialogs.EditDialog;
import com.commeto.kuleuven.MP.http.GetTask;
import com.commeto.kuleuven.MP.interfaces.AsyncResponseInterface;
import com.commeto.kuleuven.MP.http.HTTPStatic;
import com.commeto.kuleuven.MP.http.PostTask;
import com.commeto.kuleuven.MP.interfaces.EditDialogInterface;
import com.commeto.kuleuven.MP.listeners.UnderlineButtonListener;
import com.commeto.kuleuven.MP.R;
import com.commeto.kuleuven.MP.sqlSupport.LocalDatabase;
import com.commeto.kuleuven.MP.sqlSupport.LocalRoute;
import com.commeto.kuleuven.MP.support.ExternalIO;
import com.commeto.kuleuven.MP.support.InternalIO;
import com.commeto.kuleuven.MP.support.MapSupport;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import static com.commeto.kuleuven.MP.http.HTTPStatic.convertInputStreamToString;
import static com.commeto.kuleuven.MP.http.HTTPStatic.getRouteJson;
import static com.commeto.kuleuven.MP.support.NotifyStatic.postNotification;
import static com.commeto.kuleuven.MP.support.Static.isNetworkAvailable;
import static com.commeto.kuleuven.MP.support.Static.makeToastLong;
import static com.commeto.kuleuven.MP.support.Static.timeFormat;

/**
 * Created by Jonas on 12/03/2018.
 *
 * Activity to display the information from the rides. The activity is used in combination with
 * the RideListFragment as well as to display the information after a user has just finished.
 *
 * Uses:
 *  - DetailsFragment
 *  - EditDialog
 *  - DescriptionDialog
 *  - ExternalIO
 */

public class RideDisplayActivity extends AppCompatActivity implements OnMapReadyCallback {

//==================================================================================================
    //class specs

    private Context context;

    /**
     * Interface used to save the snapped coordinates pulled from the server.
     */
    private AsyncResponseInterface snappedInterface = new AsyncResponseInterface() {
        @Override
        public void processFinished(HTTPResponse response) {
            if(response.getResponseCode() == 200){
                try {
                    JSONObject object = new JSONObject(response.getResponseBody());
                    InternalIO.writeToInternal(
                            context,
                            Integer.toString(localRoute.getLocalId()) + "_snapped.json",
                            object.getString("jsonArraySnapped"),
                            false
                    );
                    InternalIO.writeToInternal(
                            context,
                            Integer.toString(localRoute.getLocalId()) + "_unsnapped.json",
                            object.getString("jsonArrayUnsnapped"),
                            false
                    );
                } catch (JSONException e){
                    InternalIO.writeToLog(context, e);
                }
            }
        }
    };

    /**
     * Interface used to save the unsnapped coordinates pulled from the server.
     */
    private EditDialogInterface editDialogInterface = new EditDialogInterface() {
        @Override
        public void changeDescription(String rideName, String descriptions) {
            if(!localRoute.getDescription().equals(descriptions) || !localRoute.getRidename().equals(rideName)){
                localRoute.setDescription(descriptions);
                localRoute.setRidename(rideName);
                localRoute.setUpdated(true);
                localRoute.setLastUpdated(System.currentTimeMillis());
                LocalDatabase.getInstance(context).localRouteDAO().update(localRoute);
                route_name.setText(localRoute.getRidename());
            }
        }
    };

    private LocalRoute localRoute;

    private MapView mapView;
    private TextView route_name;

    private boolean generated;
    private String toWrite;

    //==================================================================================================
    //lifecycle

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_ride_display);
        context = getApplicationContext();

        generated = false;

        List<LocalRoute> localRoutes = LocalDatabase.getInstance(getApplicationContext()).localRouteDAO().exists(
                getIntent().getIntExtra("id", 0),
                getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE).getString("username", "")
        );
        if(localRoutes.size() > 0) localRoute = localRoutes.get(0);
        else finish();

        route_name = findViewById(R.id.ride_name);

        route_name.setText(localRoute.getRidename());
        ((EditText) findViewById(R.id.route_name_edit)).setText(localRoute.getRidename());
        Mapbox.getInstance(context, getResources().getString(R.string.jern_key));
        mapView = findViewById(R.id.map);
        mapView.setStyleUrl(getString(R.string.mapbox_style_mapbox_streets));
        mapView.onCreate(bundle);
    }

    @Override
    public void onStart(){
        super.onStart();
        mapView.onStart();
        setDetails();

        findViewById(R.id.edit).setOnTouchListener(new UnderlineButtonListener(context));
        findViewById(R.id.delete).setOnTouchListener(new UnderlineButtonListener(context));
        findViewById(R.id.export).setOnTouchListener(new UnderlineButtonListener(context));
        findViewById(R.id.description).setOnTouchListener(new UnderlineButtonListener(context));
        findViewById(R.id.show_anyway).setOnTouchListener(new UnderlineButtonListener(context));

        if(localRoute.getType().equals("void") || localRoute.getType().equals("")) optionDialog();

        getSnapped();
        setMap();
    }

    @Override
    public void onResume(){
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop(){
        super.onStop();
        mapView.onStop();

        setResult(RESULT_OK);
        sync();
    }

    @Override
    public void onDestroy(){

        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
//==================================================================================================
    //private functions

    /**
     * Called at onStop to see if any changes have been made to the ride or if the ride has not yet
     * been sent. The ride will be either updated or uploaded accordingly.
     */
    private void sync(){
        if(localRoute.getId() == -1 && !localRoute.isSent()){
            upload(false);
        } else if(localRoute.isUpdated()){
            updatePush();
        }
    }

    /**
     * Used to fill in the DetailsFragment after it has been initialised.
     */
    private void setDetails(){

        ((TextView) findViewById(R.id.average_speed)).setText(
                String.format(Locale.getDefault(), "%.1f km/h", localRoute.getSpeed() * 3.6)
        );
        double distance = localRoute.getDistance();
        ((TextView) findViewById(R.id.total_distance)).setText(
                distance > 1000 ?
                        String.format(Locale.getDefault(), "%.2f km", distance / 1000) :
                        String.format(Locale.getDefault(), "%.2f m", distance)
        );
        ((TextView) findViewById(R.id.date)).setText(
                DateFormat.getDateTimeInstance().format(new Date(localRoute.getTime()))
        );
        int[] time = timeFormat(localRoute.getDuration());
        ((TextView) findViewById(R.id.duration)).setText(time.length == 3 ?
                String.format(Locale.getDefault(), "%d uur, %d min, %d sec", time[0], time[1], time[2]) :
                String.format(Locale.getDefault(),"%d min, %d sec", time[0], time[1])
        );
    }

    /**
     * Used to initiate the map to display the ride. Before displaying the internet connectivity
     * will be checked.
     *
     * <p>
     *     possibilities:
     * </p>
     * <ul>
     *     <li>
     *         wifi connection: Display map.
     *     </li>
     *     <li>
     *         mobile data:     Ask before displaying.
     *     </li>
     *     <li>
     *         no connection:   Ask before trying to display, map might not work fully.
     *     </li>
     * </ul>
     */
    private void setMap(){

        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (wifi.isConnected()) {
                showAnyway(null);
            } else{

                NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (mobile.isConnected()) {
                    ((TextView) findViewById(R.id.message)).setText(getString(R.string.on_4g));
                    ((TextView) findViewById(R.id.message_button)).setText(getString(R.string.show_anyway));

                } else{
                    ((TextView) findViewById(R.id.message)).setText(getString(R.string.no_connect));
                    ((TextView) findViewById(R.id.message_button)).setText(getString(R.string.try_anyway));
                }
            }
        } catch (NullPointerException e){
            InternalIO.writeToLog(context, e);
        }
    }

    /**
     * Used to push a notification as well re enable the button to sync the ride.
     *
     * @param title   The title the notification has to have.
     * @param message The message the notification has to have?
     */
    private void endNotify(String title, String message){

        findViewById(R.id.export).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                export(view);
            }
        });
        postNotification(this, title, message);
    }

    /**
     * Used to update the ride on the server. This will generate a PostTask and a JSON file
     * containing the info that can be updated, namely the name and the description of if the ride
     * has to be deleted. The ride's id is used in the url to tell the server which ride to update.
     */
    private void updatePush(){

        if(isNetworkAvailable(context)){

            SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);

            postNotification(this, "Update", localRoute.getRidename() + " updaten...");

            PostTask update = new PostTask(
                    preferences.getString(getString(R.string.preferences_ip), getString(R.string.hard_coded_ip)) + ":" + preferences.getString(getString(R.string.preferences_socket), getString(R.string.hard_coded_socket)),
                    getString(R.string.push) + Integer.toString(localRoute.getId()) + "/" + Long.toString(localRoute.getLastUpdated()),
                    updateInterface,
                    localRoute.getLocalId(),
                    null
            );

            try {
                JSONObject updateObject = new JSONObject();
                updateObject.put("name", localRoute.getRidename());
                updateObject.put("description", localRoute.getDescription());
                updateObject.put("deleted", false);

                update.execute(updateObject.toString(),
                        preferences.getString("username", ""),
                        preferences.getString("token", "")
                );
            } catch (Exception e){
                InternalIO.writeToLog(context, e);
                endNotify(getString(R.string.end_update), e.getMessage());
            }
        } else {
            endNotify(getString(R.string.no_internet), getString(R.string.no_internet_message));
        }
    }

    /**
     * Used to upload the ride to the server. Normally this function should block the ride from
     * uploading if the ride has already been sent, but an override has been added for debugging
     * purposes.
     *
     * @param override An override to force an upload.
     */
    private void upload(boolean override){

        if(isNetworkAvailable(context)){

            SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
            if(!localRoute.isSent() || override) {

                postNotification(this, "Uploaden", localRoute.getRidename() + " uploaden...");

                PostTask upload = new PostTask(
                        preferences.getString(getString(R.string.preferences_ip), getString(R.string.hard_coded_ip)) + ":" + preferences.getString(getString(R.string.preferences_socket), getString(R.string.hard_coded_socket)),
                        getString(R.string.measurement),
                        uploadInterface,
                        localRoute.getLocalId(),
                        null
                );
                try {
                    upload.execute(getRouteJson(context, localRoute),
                            preferences.getString("username", ""),
                            preferences.getString("token", "")
                    );
                } catch (Exception e){
                    InternalIO.writeToLog(context, e);
                    endNotify(getString(R.string.end_upload), e.getMessage());
                }
            } else {
                endNotify(getString(R.string.end_upload), getString(R.string.already_uploaded));
            }
        } else {
            endNotify(getString(R.string.no_internet), getString(R.string.no_internet_message));
        }
    }

    /**
     * Save all the data that was collected while riding. Ride info is not added in this file.
     * Potentially no full file might be available anymore, as the ride might have been pulled
     * from the server. The user will be notified of this.
     */
    private void saveJsonFull(){

        try {
            toWrite = convertInputStreamToString(
                    InternalIO.getInputStream(context, localRoute.getLocalId() + ".json")
            );
            ExternalIO.createFile(this, "text/json", localRoute.getRidename() + ".json");
        } catch (Exception e){
            makeToastLong(context, "geen full meer beschikbaar.");
        }
    }

    /**
     * Save the JSON file that would be used in the POST message to the server.
     */
    private void saveJsonPost(){

        try {
            toWrite = HTTPStatic.getRouteJson(context, localRoute);

            ExternalIO.createFile(this, "text/json", localRoute.getRidename() + "_post.json");
        } catch (Exception e){
            InternalIO.writeToLog(context, e);
        }
    }

    /**
     * Save the file in gpx format.
     */
    private void saveGPX(){

        try{

            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

            Element gpx = doc.createElement("gpx");
            Element name = doc.createElement("name");
            name.setTextContent(localRoute.getRidename());
            gpx.appendChild(name);
            Element distanceTag = doc.createElement("distance");
            gpx.appendChild(distanceTag);
            Element trk = doc.createElement("trk");
            Element trkseg = doc.createElement("trkseg");

            JSONObject object = new JSONObject(convertInputStreamToString(
                    InternalIO.getInputStream(context, localRoute.getLocalId() + ".json")
            ));

            Element trkpt, ele;
            JSONArray array = object.getJSONArray("measurements");
            for(int i  = 0; i < array.length(); i++){

                trkpt = doc.createElement("trkpt");
                trkpt.setAttribute(
                        "lat",
                        Double.toString(array.getJSONObject(i).getDouble("lat"))
                );
                trkpt.setAttribute(
                        "lon",
                        Double.toString(array.getJSONObject(i).getDouble("lon"))
                );
                ele = doc.createElement("ele");
                ele.setTextContent(Double.toString(array.getJSONObject(i).getDouble("ele")));

                trkpt.appendChild(ele);
                trkseg.appendChild(trkpt);
            }

            trk.appendChild(trkseg);
            gpx.appendChild(trk);

            Transformer tf = TransformerFactory.newInstance().newTransformer();
            tf.setOutputProperty(OutputKeys.ENCODING, "utf8");
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            tf.setParameter(OutputKeys.CDATA_SECTION_ELEMENTS, doc);
            StringWriter stringWriter = new StringWriter();
            StreamResult streamResult = new StreamResult(stringWriter);
            tf.transform(new DOMSource(gpx), streamResult);

            toWrite = stringWriter.toString();
            ExternalIO.createFile(this, "text/gpx", localRoute.getRidename() + ".gpx");
        } catch (Exception e){
            makeToastLong(context, "geen full meer beschikbaar.");
        }
    }

    /**
     * An optional component used to ask the user to specify a ride type in case no type was
     * found in the database.
     */
    private void optionDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.option_title))
                .setItems(R.array.options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String[] options = getResources().getStringArray(R.array.options);
                        localRoute.setType(options[i]);
                        LocalDatabase.getInstance(context).localRouteDAO().update(localRoute);
                        setResult(RESULT_OK);
                    }
                });
        builder.show();
    }

    private void block(View view){
        makeToastLong(context, "Bezig...");
    }
//==================================================================================================
    //map methods

    /**
     * Override for the onMapReadyCallback.
     *
     * @param map The initiated map.
     */
    @Override
    public void onMapReady(MapboxMap map){

        MapSupport mapSupport = new MapSupport(context, localRoute);

        mapSupport.displayRide(map);
        map.setLatLngBoundsForCameraTarget(mapSupport.getBounds());
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(mapSupport.getBounds(), 20));
    }
//==================================================================================================
    //AsyncResponseInterface

    /**
     * Interface used to handle the result when a ride has to be deleted.
     */
    private AsyncResponseInterface deleteInterface = new AsyncResponseInterface() {
        @Override
        public void processFinished(HTTPResponse response) {
            if(response.getResponseCode() == 200){
                try {
                    if (localRoute.getLocalId() == response.getId() &&
                            new JSONObject(response.getResponseBody()).getString("code").equals("ok")) {
                        LocalDatabase.getInstance(context).localRouteDAO().delete(localRoute);
                        finish();
                    }
                } catch (JSONException e){
                    endNotify("Delete", "Verwijderen van " + localRoute.getRidename() + " gefaald.");
                }
            }
        }
    };

    /**
     * Interface used to handle the result when a ride has been uploaded.
     */
    private AsyncResponseInterface uploadInterface = new AsyncResponseInterface() {
        @Override
        public void processFinished(HTTPResponse response) {
            if (response.getResponseCode() == 200){

                int id = Integer.parseInt(response.getResponseBody());
                if (id != -1 && localRoute.getLocalId() == response.getId()) {
                    LocalDatabase database = LocalDatabase.getInstance(getApplicationContext());
                    localRoute.setSent(true);
                    localRoute.setId(Integer.parseInt("2"));
                    database.localRouteDAO().update(localRoute);

                    endNotify("Einde upload", localRoute.getRidename() + " succesvol geupload.");
                } else endNotify("Einde upload", localRoute.getRidename() + " niet geupload.");
            } else {
                endNotify("Einde upload", "upload " + localRoute.getRidename() + " gefaald.");
            }
        }
    };

    /**
     * Interface to handle the result when a ride has been updated.
     */
    private AsyncResponseInterface updateInterface = new AsyncResponseInterface() {
        @Override
        public void processFinished(HTTPResponse response) {
            if (response.getResponseCode() == 200){

                try {
                    JSONObject responseObject = new JSONObject(response.getResponseBody());

                    if(responseObject.get("code").equals("ok") && localRoute.getLocalId() == response.getId()) {
                        localRoute.setUpdated(false);
                        localRoute.setLastUpdated(System.currentTimeMillis());
                        LocalDatabase.getInstance(context).localRouteDAO().update(localRoute);
                        endNotify("Einde update", localRoute.getRidename() + " succesvol geupdate.");
                    } else{
                        endNotify("Einde update", "update " + localRoute.getRidename() + " gefaald.");
                    }
                } catch (Exception e){
                    InternalIO.writeToLog(context,e);
                    endNotify("Einde update", "update " + localRoute.getRidename() + " gefaald.");
                }
            } else {
                endNotify("Einde update", "update " + localRoute.getRidename() + " gefaald.");
            }
        }
    };

    /**
     * Method to get the snapped and unsnapped coordinates from the server.
     */
    private void getSnapped(){

        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        new GetTask(
                preferences.getString("baseUrl", getString(R.string.hard_coded_ip)) + ":" + preferences.getString("socket", getString(R.string.hard_coded_socket)),
                "/MP/service/secured/ride/pull/" + Integer.toString(localRoute.getId()) + "/0",
                snappedInterface,
                null,
                -1
        ).execute(
                preferences.getString("username", ""),
                preferences.getString("token", "")
        );
    }
//==================================================================================================
    //buttons

    /**
     * Button action to display the map in case setMap() has blocked it's initiation.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void showAnyway(View view){

        if(!generated) {
            generated = true;
            findViewById(R.id.map_container).setVisibility(View.VISIBLE);
            findViewById(R.id.message_container).setVisibility(View.GONE);
            mapView.getMapAsync(this);
        }
    }

    /**
     * Button action to show a dialog with options for how to export the ride in a certain format.
     * <ul>
     *     <li>
     *         full json
     *     </li>
     *     <li>
     *         post json
     *     </li>
     *     <li>
     *         gpx
     *     </li>
     *     <li>
     *         posting the ride to the server
     *     </li>
     *     <li>
     *         posting the ride to the server with override.
     *     </li>
     * </ul>
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void export(View view){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exporteren naar: ")
                .setItems(R.array.export_spinner, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String option = context.getResources().getStringArray(R.array.export_spinner)[i];

                        switch(option){
                            case "json (full)":
                                saveJsonFull();
                                break;
                            case "json (post)":
                                saveJsonPost();
                                break;
                            case "gpx":
                                saveGPX();
                                break;
                            case "post to server":
                                upload(false);
                                break;
                            case "post to server (override)":
                                upload(true);
                                break;
                            default:
                                saveJsonPost();
                                break;
                        }
                    }
                });
        builder.create().show();
    }

    /**
     * Button action to display a DescriptionDialog to show the rides description.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void description(View view){

        DescriptionDialog descriptionDialog = new DescriptionDialog();
        descriptionDialog.set(localRoute.getDescription());
        descriptionDialog.show(getSupportFragmentManager(), "Description");
    }

    /**
     * Button to open a FullScreenActivity to display the ride as fullscreen.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void fullscreenMap(View view){
        startActivity(new Intent(this, FullScreenActivity.class)
                .putExtra("id", localRoute.getLocalId())
        );
    }

    /**
     * Button action to diaplay an EditDialog to be able to edit the ride's name as well as the
     * description. an interface is used to handle the response.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void edit(final View view){

        EditDialog editDialog = new EditDialog();
        editDialog.set(localRoute.getRidename(), localRoute.getDescription(), editDialogInterface);
        editDialog.show(getSupportFragmentManager(), "Edit");
    }

    /**
     * Button action to delete the ride. A confirmation dialog will be shown to confirm the action
     * before deletion. Afterwards a post message will be sent with a json containing a boolean
     * 'true' in it's field 'deleted'.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void delete(View view){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Wilt u deze rit verwijderen?");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(localRoute.isSent()){
                    if(isNetworkAvailable(context)){
                        try {
                            SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
                            JSONObject toWrite = new JSONObject();
                            toWrite.put("name", "delete pls");
                            toWrite.put("description", "pretty please");
                            toWrite.put("deleted", true);
                            new PostTask(
                                    preferences.getString("baseUrl", getString(R.string.hard_coded_ip)) + ":" + preferences.getString("socket", getString(R.string.hard_coded_socket)),
                                    "/MP/service/secured/ride/push/" + Integer.toString(localRoute.getId()) + "/0",
                                    deleteInterface,
                                    localRoute.getLocalId(),
                                    null
                            ).execute(
                                    toWrite.toString(),
                                    preferences.getString("username", ""),
                                    preferences.getString("token", "")
                            );
                        }catch (Exception e){
                            InternalIO.writeToLog(context, e);
                        }
                    } else {
                        makeToastLong(context, getString(R.string.no_server));
                    }
                } else {
                    LocalDatabase.getInstance(context).localRouteDAO().delete(localRoute);
                    setResult(RESULT_OK);
                    finish();
                }

                if(dialogInterface != null) dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(dialogInterface != null) dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }
//==================================================================================================
    //activity result

    /**
     * onActivity result to handle the saving of files.
     *
     * @param requestCode The requestCode of the calling intent.
     * @param resultCode  The returned resultCode.
     * @param data        The returned resultData.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){

        if(resultCode == RESULT_OK){
            if(requestCode != 580) {
                ExternalIO.alterDocument(context, toWrite, requestCode, resultCode, data);
            }
        }
    }
}
