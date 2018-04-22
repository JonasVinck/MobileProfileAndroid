package com.commeto.kuleuven.MP.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.commeto.kuleuven.MP.dataClasses.HTTPResponse;
import com.commeto.kuleuven.MP.http.PostTask;
import com.commeto.kuleuven.MP.interfaces.AsyncResponseInterface;
import com.commeto.kuleuven.MP.R;
import com.commeto.kuleuven.MP.sqlSupport.LocalDatabase;
import com.commeto.kuleuven.MP.sqlSupport.LocalRoute;
import com.commeto.kuleuven.MP.support.ExternalIO;
import com.commeto.kuleuven.MP.support.InternalIO;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.commeto.kuleuven.MP.http.HTTPStatic.convertInputStreamToString;
import static com.commeto.kuleuven.MP.http.HTTPStatic.getRouteJson;
import static com.commeto.kuleuven.MP.support.Static.makeToastLong;

/**
 * Created by Jonas on 1/03/2018.
 */

public class RideFinishedActivity extends AppCompatActivity implements AsyncResponseInterface{
//==================================================================================================
    //class specs

    private Context context;
    private LocalRoute localRoute;

    private NotificationManagerCompat notificationManagerCompat;
    private NotificationCompat.Builder notificationBuilder;

//==================================================================================================
    //lifecycle methods

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_ride_finished);
        context = getApplicationContext();

        localRoute = LocalDatabase.getInstance(getApplicationContext()).localRouteDAO().exists(
                getIntent().getIntExtra("id", 0),
                getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE).getString("username", "")
        ).get(0);

        ((EditText) findViewById(R.id.ride_name)).setText(localRoute.getRidename());
        ((TextView) findViewById(R.id.average_speed)).setText(String.format(
                Locale.getDefault(),
                "%.2f",
                localRoute.getSpeed() * 3.6
        ));
        double distance = localRoute.getDistance();
        if(distance > 1000) distance = distance/1000;
        String distanceString = String.format(
                Locale.getDefault(),
                "%.2f",
                distance
        ) + (localRoute.getDistance() > 1000 ? "km":"m");
        ((TextView) findViewById(R.id.average_speed)).setText(distanceString);
        ((TextView) findViewById(R.id.date)).setText(
                new SimpleDateFormat(
                        "dd-MM-yyyy, HH:mm:ss",
                        Locale.getDefault()
                ).format(new Date(localRoute.getTime())
        ));
        ((TextView) findViewById(R.id.duration)).setText(
                new SimpleDateFormat(
                        "HH:mm:ss",
                        Locale.getDefault()
                ).format(new Date(localRoute.getDuration()))
        );
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
//==================================================================================================
    //AsyncResponseInterface

    @Override
    public void processFinished(HTTPResponse response){
        if(response != null){
            if(response.getResponseCode() == 200){
                String[] responseBody = response.getResponseBody().split(",");
                if(!responseBody[0].equals("-1")){
                    int id = Integer.parseInt(responseBody[0]);
                    LocalDatabase database = LocalDatabase.getInstance(getApplicationContext());
                    database.localRouteDAO().updateSent(id, true);
                    database.localRouteDAO().updateRideId(id, Integer.parseInt(responseBody[1]));
                    makeToastLong(getApplicationContext(), localRoute.getRidename() + " geupload.");
                }
            } else {
                makeToastLong(getApplicationContext(), localRoute.getRidename() + " niet geupload.");
            }
        } else{
            notificationBuilder.setContentTitle("Einde upload")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setContentText("Fout bij uploaden van " + localRoute.getRidename());
            notificationManagerCompat.notify(getString(R.string.preferences), 1, notificationBuilder.build());
        }
        finish();
    }
//==================================================================================================
    //button actions

    public void confirm(View view){
        if(!((EditText) findViewById(R.id.ride_name)).getText().toString()
                .equals(localRoute.getRidename())){
            localRoute.setRidename(((EditText) findViewById(R.id.ride_name)).getText().toString());

            LocalDatabase.getInstance(context).localRouteDAO().updateRideName(
                    localRoute.getId(),
                    localRoute.getRidename()
            );
        }

        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        if(preferences.getBoolean("auto_upload", false)) {

            notificationManagerCompat = NotificationManagerCompat.from(context);
            notificationBuilder = new NotificationCompat.Builder(context, getString(R.string.preferences));
            notificationBuilder.setContentTitle("rit uploaden")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setContentText(localRoute.getRidename() + " uploaden...");
            notificationManagerCompat.notify(getString(R.string.preferences), 1, notificationBuilder.build());

            PostTask upload = new PostTask(
                    preferences.getString("baseUrl", getString(R.string.hard_coded_ip)) + ":" + preferences.getString("socket", getString(R.string.hard_coded_socket)),
                    "/MP/service/secured/measurement",
                    this,
                    localRoute.getLocalId(),
                    null
            );

            try {
                upload.execute(
                        getRouteJson(getApplicationContext(), localRoute),
                        preferences.getString("username", ""),
                        preferences.getString("token", "")
                );
            } catch (Exception e){
                InternalIO.writeToLog(context, e);
            }
        } else{
            finish();
        }

        if(preferences.getBoolean("export_full", false)) {
            ExternalIO.createFile(this, "text/json", localRoute.getRidename() + ".json");
        }
    }
//==================================================================================================
    //io functions

    @Override
    public void onActivityResult(int requestcode, int resultcode, Intent resultData){

        try {
            ExternalIO.alterDocument(
                    context,
                    convertInputStreamToString(
                            InternalIO.getInputStream(context, localRoute.getLocalId() + ".json")
                    ),
                    requestcode,
                    resultcode,
                    resultData
            );
        } catch (Exception e){
            makeToastLong(context, "Er ging iets mis");
            InternalIO.writeToLog(context, e);
        }
    }
}
