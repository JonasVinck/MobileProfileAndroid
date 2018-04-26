package com.commeto.kuleuven.MP.http;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;

import com.commeto.kuleuven.MP.dataClasses.HTTPResponse;
import com.commeto.kuleuven.MP.interfaces.AsyncResponseInterface;

import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import static com.commeto.kuleuven.MP.http.HTTPStatic.convertInputStreamToString;

/**
 * <pre>
 * Created by Jonas on 28/02/2018.
 *
 * task used for HTTP POST messages.
 * </pre>
 */

public class PostTask extends AsyncTask<String, Void, Boolean> implements HostnameVerifier{

    private Integer id;
    private String baseUrl;
    private String url;
    private HTTPResponse result;
    private AsyncResponseInterface asyncResponseInterface;
    private Bundle options;

    /**
     * @param fullip Full IP address of the server.
     * @param url Path to send the message to?
     * @param responseInterface Interface used for response.
     * @param id Id of the possibly specified route being uploaded.
     * @param options Possible options to be put in the url.
     */

    public PostTask(String fullip, String url, AsyncResponseInterface responseInterface, int id, Bundle options){
        this.baseUrl = fullip;
        this.url = url;
        this.id = id;
        this.asyncResponseInterface = responseInterface;
        this.options = options;
    }
//==================================================================================================
    //interface override

    //Bad implementation to use HTTPS.
    @Override
    public boolean verify(String s, SSLSession sslSession) {
        HostnameVerifier hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
        return hostnameVerifier.verify(s, sslSession) || s.equals(baseUrl.split(":")[0]);
    }
//==================================================================================================
    //asynctask override

    @Override
    protected Boolean doInBackground(String... strings) {

        boolean done;

        try {
            //TODO
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
                    repsonse,
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
