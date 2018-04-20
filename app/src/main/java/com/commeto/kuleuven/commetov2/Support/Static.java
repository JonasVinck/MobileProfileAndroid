package com.commeto.kuleuven.commetov2.Support;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.commeto.kuleuven.commetov2.DataClasses.HTTPResponse;
import com.commeto.kuleuven.commetov2.HTTP.GetTask;
import com.commeto.kuleuven.commetov2.Interfaces.AsyncResponseInterface;
import com.commeto.kuleuven.commetov2.SQLSupport.LocalDatabase;
import com.commeto.kuleuven.commetov2.SQLSupport.LocalRoute;

import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Created by Jonas on 1/03/2018.
 */

public class Static {

    public static int[] timeFormat(long time){

        int hours, minutes, seconds;

        seconds = (int) (time / 1000);
        minutes = seconds / 60;
        hours = minutes / 60;
        minutes = minutes % 60;
        seconds = seconds % 60;

        if(hours > 0) return new int[]{hours, minutes, seconds};
        else return new int[]{minutes, seconds};
    }

    public static String timeToString(long time){
        int[] temp = timeFormat(time);
        String minutes;

        if(temp.length == 3){
            minutes = temp[1] > 9 ? Integer.toString(temp[1]) : "0" + Integer.toString(temp[1]);
            return Integer.toString(temp[0]) + ":" + minutes;
        } else {
            minutes = temp[0] > 9 ? Integer.toString(temp[0]) : "0" + Integer.toString(temp[0]);
            return "00:" + minutes;
        }
    }

    public static String timeToStringWithSeconds(long time){
        int[] temp = timeFormat(time);
        String minutes, seconds;

        if(temp.length == 3){
            minutes = temp[1] > 9 ? Integer.toString(temp[1]) : "0" + Integer.toString(temp[1]);
            seconds = temp[2] > 9 ? Integer.toString(temp[2]) : "0" + Integer.toString(temp[2]);
            return Integer.toString(temp[0]) + ":" + minutes + ":" + seconds;
        } else {
            minutes = temp[0] > 9 ? Integer.toString(temp[0]) : "0" + Integer.toString(temp[0]);
            seconds = temp[1] > 9 ? Integer.toString(temp[1]) : "0" + Integer.toString(temp[1]);
            return "00:" + minutes + ":" + seconds;
        }
    }

    public static void tryLogin(Context context, AsyncResponseInterface loginSucces){

        if(isNetworkAvailable(context)) {
            SharedPreferences preferences = context.getSharedPreferences("commeto", Context.MODE_PRIVATE);
            preferences.edit().putBoolean("offline", false).apply();
            GetTask loginTask = new GetTask(
                    preferences.getString("baseUrl", "213.118.13.252") + ":" + preferences.getString("socket", "443"),
                    "/MP/service/secured/tokenvalid",
                    loginSucces,
                    null,
                    -1
            );
            loginTask.execute(
                    preferences.getString("username", ""),
                    preferences.getString("token", "")
            );
        } else loginSucces.processFinished(new HTTPResponse());
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo;
        if(connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        } else activeNetworkInfo = null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void makeToastLong(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void makeToastShort(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static int scaleMenuIcon(){
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        return displayMetrics.widthPixels / 5;
    }

    public static LinearLayout.LayoutParams getLayoutparams(Context context, int id){

        Drawable bitmap = context.getResources().getDrawable(id);
        DisplayMetrics dm  = Resources.getSystem().getDisplayMetrics();
        double factor = (double) bitmap.getIntrinsicHeight() / (double) bitmap.getIntrinsicWidth();

        return new LinearLayout.LayoutParams(
                dm.widthPixels, (int) (factor * dm.widthPixels)
        );
    }

    public static int getIDInteger(Context context) {

        LocalDatabase localDatabase = LocalDatabase.getInstance(context);
        boolean done = false;
        int id = 0;
        while (!done){

            done = true;
            id = new Random().nextInt(999999);

            List<LocalRoute> result = localDatabase.localRouteDAO().exists(id);
            done = (result.size() == 0);
        }

        return id;
    }
}
