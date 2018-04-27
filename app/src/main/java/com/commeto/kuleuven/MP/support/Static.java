package com.commeto.kuleuven.MP.support;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.commeto.kuleuven.MP.R;
import com.commeto.kuleuven.MP.dataClasses.HTTPResponse;
import com.commeto.kuleuven.MP.http.GetTask;
import com.commeto.kuleuven.MP.interfaces.AsyncResponseInterface;
import com.commeto.kuleuven.MP.sqlSupport.LocalDatabase;
import com.commeto.kuleuven.MP.sqlSupport.LocalRoute;

import java.util.List;
import java.util.Random;

/**
 * <pre>
 * Created by Jonas on 1/03/2018.
 *
 * Static helper functions.
 * </pre>
 */

public class Static {

    /**
     * Method to generate integer values for hours, minutes and seconds from Long time.
     *
     * @param time Given time.
     * @return Integer values.
     */

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

    /**
     * Method to generate String from Long time.
     *
     * @param time Given time.
     * @return Time as String.
     */

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

    /**
     * Method to generate String, including seconds, from Long time.
     *
     * @param time Given time.
     * @return Time as String.
     */

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

    /**
     * Method to see if token is still valid.
     *
     * @param context Application context.
     * @param loginSucces Interface to be used.
     */

    public static void tryLogin(Context context, AsyncResponseInterface loginSucces){

        if(isNetworkAvailable(context)) {
            SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preferences), Context.MODE_PRIVATE);
            preferences.edit().putBoolean("offline", false).apply();
            GetTask loginTask = new GetTask(
                    preferences.getString(context.getString(R.string.preferences_ip), context.getString(R.string.hard_coded_ip)) + ":" + preferences.getString(context.getString(R.string.preferences_socket), context.getString(R.string.hard_coded_socket)),
                    context.getString(R.string.token_valid),
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

    /**
     * Method used to check if connection is available.
     *
     * @param context Application context.
     * @return Boolean to represent availability.
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo;
        if(connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        } else activeNetworkInfo = null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Method to generate long toasts.
     *
     * @param context Application context.
     * @param message Message to display.
     */

    public static void makeToastLong(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Method to generate short toasts.
     *
     * @param context Application context.
     * @param message Message to display.
     */

    public static void makeToastShort(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Method to get base width of menu icons in BaseActivity.
     *
     * @return Integer for the base scale.
     */

    public static int scaleMenuIcon(){
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        return displayMetrics.widthPixels / 5;
    }

    /**
     * Method used to set an images width to the screen width.
     *
     * @param context Application context.
     * @param id      Id if the image's drawable.
     * @return        LayoutParams for ImageView.
     */

    public static LinearLayout.LayoutParams getLayoutParams(Context context, int id){

        Drawable bitmap = context.getResources().getDrawable(id);
        DisplayMetrics dm  = Resources.getSystem().getDisplayMetrics();
        double factor = (double) bitmap.getIntrinsicHeight() / (double) bitmap.getIntrinsicWidth();

        return new LinearLayout.LayoutParams(
                dm.widthPixels, (int) (factor * dm.widthPixels)
        );
    }

    /**
     * Method to generate new random local id.
     *
     * @param context The calling application context.
     * @return        Integer id.
     */

    public static int getIDInteger(Context context) {

        LocalDatabase localDatabase = LocalDatabase.getInstance(context);
        boolean done = false;
        int id = 0;
        while (!done){

            id = new Random().nextInt(999999);

            List<LocalRoute> result = localDatabase.localRouteDAO().exists(id, context.getSharedPreferences(context.getString(R.string.preferences), Context.MODE_PRIVATE).getString("username", ""));
            done = (result.isEmpty());
        }

        return id;
    }
}
