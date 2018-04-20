package com.commeto.kuleuven.commetov2.HTTP;

import android.os.AsyncTask;

import com.commeto.kuleuven.commetov2.Interfaces.AsyncResponseInterface;
import com.commeto.kuleuven.commetov2.DataClasses.HTTPResponse;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

/**
 * Created by Jonas on 4/03/2018.
 */

public class CreateUserTask extends AsyncTask<JSONObject, Void, String> implements HostnameVerifier{
//==================================================================================================
    //class specs

    private HTTPResponse response;
    private String url;
    private String baseUrl;
    private AsyncResponseInterface responseInterface;
    private int id;

    public CreateUserTask(String baseUrl, String url, AsyncResponseInterface responseInterface, int id) {
        this.baseUrl = baseUrl;
        this.url = url;
        this.responseInterface = responseInterface;
        this.id = id;
    }
//==================================================================================================
    //interface override
    @Override
    public boolean verify(String s, SSLSession sslSession) {
        HostnameVerifier hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
        return hostnameVerifier.verify(s, sslSession) || s.equals(this.baseUrl);
    }
//==================================================================================================
    //overrides
    @Override
    protected String doInBackground(JSONObject... params){

        String resultString = null;
        response = null;

        try{

            URL url = new URL(
                    "https://" + baseUrl + ":8181" + this.url);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setSSLSocketFactory(SSLContext.getDefault().getSocketFactory());
            connection.setHostnameVerifier(this);
            connection.setConnectTimeout(3000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-type", "application/json");
            connection.setRequestMethod("POST");

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(params[0].toString().getBytes());
            outputStream.close();

            StringBuilder stringBuilder = new StringBuilder();
            String temp;
            InputStream inputStream;
            if(connection.getResponseCode() < 400){
                inputStream = connection.getInputStream();
            } else {
                inputStream = connection.getErrorStream();
            }
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            while ((temp = bufferedReader.readLine()) != null){
                stringBuilder.append(temp);
            }

            response = new HTTPResponse(
                    connection.getResponseCode(),
                    connection.getResponseMessage(),
                    stringBuilder.toString(),
                    this.id
            );

            resultString = connection.getResponseMessage();

            connection.disconnect();
        } catch (SocketTimeoutException e){
            response = new HTTPResponse(
                    -1,
                    "no server",
                    "",
                    this.id
            );
        }
        catch (Exception e){
            response = new HTTPResponse(
                    -2,
                    "error",
                    "",
                    this.id
            );
        }

        return resultString;
    }

    @Override
    protected void onPostExecute(String param){
        responseInterface.processFinished(response);
    }
}
