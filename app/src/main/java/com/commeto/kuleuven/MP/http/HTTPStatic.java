package com.commeto.kuleuven.MP.http;

import android.content.Context;

import com.commeto.kuleuven.MP.interfaces.SSLResponseInterface;
import com.commeto.kuleuven.MP.sqlSupport.LocalRoute;
import com.commeto.kuleuven.MP.support.InternalIO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

/**
 * Created by Jonas on 2/03/2018.
 *
 * Static functions for HTTP communication.
 */

public class HTTPStatic {

    /**
     * Method to convert an inputstream to a String.
     *
     * @param inputStream InputStream to be converted.
     * @return String made from InputStream.
     * @throws IOException Possibly thrown Exception.
     */
    public static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line;
        StringBuilder result = new StringBuilder();
        while((line = bufferedReader.readLine()) != null)
            result.append(line);

        inputStream.close();
        return result.toString();

    }

    /**
     * Method to set the used certificate.
     *
     * @param context Application context.
     * @param responseInterface Interface used for response.
     * @throws Exception Possibly thorwn Exception.?
     */
    public static void setCertificate(Context context, SSLResponseInterface responseInterface) throws Exception{

        int id = context.getResources().getIdentifier(
                "s1as","raw", context.getPackageName()
        );

        InputStream inputStream = context.getResources().openRawResource(id);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

        Certificate certificate = certificateFactory.generateCertificate(inputStream);
        SSLTask sslTask = new SSLTask(responseInterface, certificate);
        sslTask.execute();
    }

    /**
     * Method used to generate a JSON representation of a ride to use in the post message.
     *
     * @param context Application context.
     * @param localRoute Route to use to generate JSON.
     * @return String representing the JSON.
     * @throws JSONException Possibly thrown Exception.
     * @throws IOException Possibly thrown Exception.
     */
    public static String getRouteJson(Context context, LocalRoute localRoute) throws JSONException, IOException{

        JSONObject toWrite;

        toWrite = new JSONObject();
        toWrite.put("name", localRoute.getRidename());
        toWrite.put("description", localRoute.getDescription());
        toWrite.put("distance", localRoute.getDistance());
        try {
            toWrite.put("avSpeed", localRoute.getSpeed());
        } catch (JSONException e) {
            toWrite.put("avSpeed", 0);
        }
        toWrite.put("timeStamp", localRoute.getTime());
        toWrite.put("duration", localRoute.getDuration());
        toWrite.put("id", localRoute.getLocalId());
        try {
            toWrite.put("calibration", localRoute.getCalibration());
        } catch (Exception e){
            toWrite.put("calibration", -1);
        }
        if(localRoute.getType().equals("") || localRoute.getType().equals("void")){
            toWrite.put("type", "plezier");
        } else toWrite.put("type", localRoute.getType().toLowerCase());


        StringBuilder stringBuilder = new StringBuilder();
        String string;
        try {
            InputStream inputStream = InternalIO.getInputStream(context, Integer.toString(localRoute.getLocalId()) + ".json");

            JSONObject writeRoute = new JSONObject(convertInputStreamToString(inputStream));
            JSONArray array = writeRoute.getJSONArray("measurements");
            JSONArray sendArray = new JSONArray();
            JSONObject object, sendObject;
            for (int i = 0; i < array.length(); i++) {

                sendObject = new JSONObject();
                object = array.getJSONObject(i);

                sendObject.put("timeStamp", object.get("time"));
                sendObject.put("latitude", object.get("lat"));
                sendObject.put("longitude", object.get("lon"));
                sendObject.put("altitude", object.get("ele"));
                try {
                    sendObject.put("accuracy", object.get("accuracy"));
                } catch (JSONException e){
                    sendObject.put("accuracy", "-1");
                }
                sendObject.put("measurement", object.get("result"));
                try {
                    sendObject.put("lightMeasurement", object.get("lightResult"));
                } catch (Exception e){
                    InternalIO.writeToLog(context, e);
                }

                sendArray.put(sendObject);
            }

            toWrite.put("measurements", sendArray);
        } catch (IOException e){
            InputStream inputStream = InternalIO.getInputStream(context, Integer.toString(localRoute.getLocalId()) + "_unsnapped.json");

            JSONArray array = new JSONArray(convertInputStreamToString(inputStream));

            JSONArray sendArray = new JSONArray();
            JSONArray nestedArray;
            JSONObject sendObject;
            for (int i = 0; i < array.length(); i++) {

                sendObject = new JSONObject();
                nestedArray = array.getJSONArray(i);

                sendObject.put("latitude", nestedArray.getDouble(1));
                sendObject.put("longitude", nestedArray.getDouble(0));

                sendObject.put("timeStamp", System.currentTimeMillis());
                sendObject.put("altitude", 400);
                sendObject.put("accuracy",1);
                sendObject.put("accuracy", -1);
                sendObject.put("measurement", -1);
                sendObject.put("lightMeasurement", -1);

                sendArray.put(sendObject);
            }

            toWrite.put("measurements", sendArray);
        }

        return toWrite.toString();
    }
}
