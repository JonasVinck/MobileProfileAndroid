package com.commeto.kuleuven.commetov2.HTTP;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.commeto.kuleuven.commetov2.DataClasses.HTTPResponse;
import com.commeto.kuleuven.commetov2.Interfaces.AsyncResponseInterface;
import com.commeto.kuleuven.commetov2.R;
import com.commeto.kuleuven.commetov2.SQLSupport.LocalDatabase;
import com.commeto.kuleuven.commetov2.SQLSupport.LocalRoute;

import java.util.List;

import static com.commeto.kuleuven.commetov2.HTTP.HTTPStatic.getRouteJson;
import static com.commeto.kuleuven.commetov2.Support.Static.makeToastLong;

/**
 * Created by Jonas on 3/03/2018.
 */

public class HTTPSendAllService extends IntentService implements AsyncResponseInterface {

//==================================================================================================
    //class specs

    private String url;

    private int amount;
    private int current;
    private int error;
    private NotificationManagerCompat notificationManagerCompat;
    private NotificationCompat.Builder notificationBuilder;

    public HTTPSendAllService() {
        super("HTTPService");
    }

    public HTTPSendAllService(String name){
        super(name);
    }
//==================================================================================================
    //intent handler

    @Override
    public void onHandleIntent(Intent intent){
    }
//==================================================================================================
    //inerface override

    @Override
    public void processFinished(HTTPResponse httpResponse){

        if(httpResponse.getResponseCode() == 200){

            String[] responseBody = httpResponse.getResponsBody().split(",");
            if(!responseBody[0].equals("-1")){
                int id = Integer.parseInt(responseBody[0]);
                LocalDatabase database = LocalDatabase.getInstance(getApplicationContext());
                database.localRouteDAO().updateSent(id, true);
                database.localRouteDAO().updateRideId(id, Integer.parseInt(responseBody[1]));
                makeToastLong(getApplicationContext(), httpResponse.getResponsBody() + " geupload.");
            } else {
                error++;
            }
        } else{
            error++;
        }

        current++;
        if(current == amount){
            notificationBuilder.setContentText("Er zijn " + (amount - error) + " van de " + amount + " ritten succesvol geupload.");
            notificationBuilder.setProgress(0,0, false);
        } else {
            notificationBuilder.setProgress(amount, current, false);
        }
        notificationManagerCompat.notify(123, notificationBuilder.build());

    }
//==================================================================================================
    //lifecycle methods

    @Override
    public void onCreate(){
        super.onCreate();

        notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationBuilder = new NotificationCompat.Builder(this, "commeto");
        notificationBuilder.setContentTitle("ritten uploaden")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_LOW);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        this.url = "https://" + getSharedPreferences("commeto", MODE_PRIVATE)  + ":8181/MP/service/test";
        try {
            LocalDatabase localDatabase = LocalDatabase.getInstance(getApplicationContext());
            List<LocalRoute> localRoutes = localDatabase.localRouteDAO().getAllNotSent();
            amount = localRoutes.size();
            current = 0;
            error = 0;
            notificationBuilder.setProgress(amount, current, false);
            notificationManagerCompat.notify(123, notificationBuilder.build());
            for (LocalRoute localRoute : localRoutes) {

                String toWrite = getRouteJson(getApplicationContext(), localRoute);
                SharedPreferences preferences = getSharedPreferences("commeto", MODE_PRIVATE);
                if(toWrite != null) {
                    PostTask upload = new PostTask(
                            preferences.getString("baseUrl", "213.118.13.252") + ":" + preferences.getString("socket", "443"),
                            "/MP/service/secured/measurement",
                            this,
                            localRoute.getId(),
                            null
                    );
                    upload.execute(toWrite, preferences.getString("username", ""), preferences.getString("token", ""));
                }
            }
        } catch (Exception e){
            makeToastLong(getApplicationContext(), e.getMessage());
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }
}
