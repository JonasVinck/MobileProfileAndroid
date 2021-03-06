package com.commeto.kuleuven.MP.http;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;

import com.commeto.kuleuven.MP.interfaces.AsyncResponseInterface;
import com.commeto.kuleuven.MP.dataClasses.HTTPResponse;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import static com.commeto.kuleuven.MP.http.HTTPStatic.convertInputStreamToString;

/**
 * <pre>
 * Created by Jonas on 4/03/2018.
 *
 * AsyncTask used to send HTTP GET messages.
 * </pre>
 */

public class GetTask extends AsyncTask<String, Void, Boolean> implements HostnameVerifier{

    private AsyncResponseInterface asyncResponseInterface;
    private String baseUrl;
    private String url;
    private HTTPResponse result;
    private Bundle options;
    private int id;

    /**
     * @param fullIp Full IP address of the server.
     * @param url Url to which message has to be sent.
     * @param asyncResponseInterface Interface used for response.
     * @param options Possible options to be put in the url.
     * @param id Possible id of the ride to be uploaded.
     */
    public GetTask(final String fullIp, String url, AsyncResponseInterface asyncResponseInterface, Bundle options, int id) {
        this.baseUrl = fullIp;
        this.url = url;
        this.asyncResponseInterface = asyncResponseInterface;
        this.options = options;
        this.id = id;
    }
//==================================================================================================
    //interface override

    //Bad solution to use HTTPS.
    @Override
    public boolean verify(String s, SSLSession sslSession) {
        HostnameVerifier hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
        return hostnameVerifier.verify(s, sslSession) || s.equals(baseUrl.split(":")[0]);
    }
//==================================================================================================
    //asynctask override

    @Override
    public Boolean doInBackground(String... params){

        result = null;
        boolean success = false;
        String temp;

        try {

            //Setting up the url
            StringBuilder urlString = new StringBuilder("https://" + baseUrl + this.url);
            if(options != null){
                urlString.append("?");
                //Options could be place in url.
                for(String key: options.keySet()){
                    urlString.append(key)
                            .append("=")
                            .append(options.getString(key));
                }
            }

            //Setting up the url connection/
            URL url = new URL(urlString.toString());
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            //Set custom hostname verifier to verify the server ip.
            urlConnection.setHostnameVerifier(this);
            urlConnection.setSSLSocketFactory(SSLContext.getDefault().getSocketFactory());
            urlConnection.setConnectTimeout(3000);
            urlConnection.setDoInput(true);
            //Add authorization to header.
            urlConnection.setRequestProperty(
                    "Authorization",
                    "Basic " + Base64.encodeToString(
                            (params[0] + ":" + params[1]).getBytes(),
                            Base64.DEFAULT
                    )
            );

            //Get normal InputStream or ErrorStream when the HTTP code is in the error range.
            InputStream inputStream;
            if(urlConnection.getResponseCode() < 400) {
                inputStream = urlConnection.getInputStream();
            } else{
                inputStream = urlConnection.getErrorStream();
            }

            temp = convertInputStreamToString(inputStream);
            inputStream.close();

            result = new HTTPResponse(
                    urlConnection.getResponseCode(),
                    urlConnection.getResponseMessage(),
                    temp,
                    id
            );

            success = true;

            //Disconnect.
            urlConnection.disconnect();
        } catch (SocketTimeoutException e){
            result = new HTTPResponse(
                    -1,
                    "no server",
                    "offline",
                    -1
            );
        }
        catch (Exception e){
            result = new HTTPResponse(
                    -2,
                    "no connection",
                    "offline",
                    -1
            );
        }

        return success;
    }

    @Override
    protected void onPostExecute(Boolean succes){

        //Return response back to calling activity.
        asyncResponseInterface.processFinished(result);
    }
}
