package com.commeto.kuleuven.commetov2.HTTP;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Base64;

import com.commeto.kuleuven.commetov2.DataClasses.HTTPResponse;
import com.commeto.kuleuven.commetov2.Interfaces.AsyncResponseInterface;

import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import static com.commeto.kuleuven.commetov2.HTTP.HTTPStatic.convertInputStreamToString;

/**
 * Created by Jonas on 28/02/2018.
 */

public class PostTask extends AsyncTask<String, Void, Boolean> implements HostnameVerifier{

    private Integer id;
    private String baseUrl;
    private String url;
    private HTTPResponse result;
    private AsyncResponseInterface asyncResponseInterface;
    private Bundle options;

    public PostTask(String baseUrl, String url, AsyncResponseInterface responseInterface, int id, Bundle options){
        this.baseUrl = baseUrl;
        this.url = url;
        this.id = id;
        this.asyncResponseInterface = responseInterface;
        this.options = options;
    }
//==================================================================================================
    //interface override

    @Override
    public boolean verify(String s, SSLSession sslSession) {
        HostnameVerifier hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
        return hostnameVerifier.verify(s, sslSession) || s.equals(baseUrl.split(":")[0]);
    }
//==================================================================================================
    //asynctask override
    @Override
    protected Boolean doInBackground(String... strings) {

        boolean done = false;

        try {
            String urlString = "https://" + this.baseUrl + this.url;
            if(options != null){
                urlString += "?";
                for(String key: options.keySet()) urlString += key + "=" + options.getString(key);
            }
            URL url = new URL(urlString);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setSSLSocketFactory(SSLContext.getDefault().getSocketFactory());
            urlConnection.setHostnameVerifier(this);
            urlConnection.setDoOutput(true);
            urlConnection.setConnectTimeout(5000);
            urlConnection.setRequestProperty("Content-Type","application/json");
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty(
                    "Authorization",
                    "Basic " + Base64.encodeToString(
                            (strings[1] + ":" + strings[2]).getBytes(),
                            Base64.DEFAULT
                    )
            );

            OutputStream outputStream = urlConnection.getOutputStream();
            outputStream.write(strings[0].getBytes());
            outputStream.flush();
            outputStream.close();

            String repsonse = convertInputStreamToString(urlConnection.getInputStream());

            result = new HTTPResponse(
                    urlConnection.getResponseCode(),
                    urlConnection.getResponseMessage(),
                    Integer.toString(id) + "," + repsonse,
                    this.id
            );

            urlConnection.disconnect();
            done = true;
        } catch (SocketTimeoutException e){
            result = new HTTPResponse(
                    -1,
                    "ERROR",
                    Integer.toString(id),
                    this.id
            );
            done = false;
        } catch (Exception e) {
            result = new HTTPResponse(
                    0,
                    "ERROR",
                    "",
                    this.id
            );
            done = false;
        }

        return done;
    }

    @Override
    protected void onPostExecute(Boolean result) {

        asyncResponseInterface.processFinished(this.result);

        super.onPostExecute(result);
    }
}
