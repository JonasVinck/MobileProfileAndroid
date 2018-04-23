package com.commeto.kuleuven.MP.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;

import com.commeto.kuleuven.MP.dataClasses.HTTPResponse;
import com.commeto.kuleuven.MP.interfaces.AsyncResponseInterface;
import com.commeto.kuleuven.MP.interfaces.SSLResponseInterface;
import com.commeto.kuleuven.MP.R;
import com.commeto.kuleuven.MP.sqlSupport.LocalDatabase;
import com.commeto.kuleuven.MP.sqlSupport.LocalRoute;
import com.commeto.kuleuven.MP.support.InternalIO;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.commeto.kuleuven.MP.http.HTTPStatic.setCertificate;
import static com.commeto.kuleuven.MP.support.Static.getLayoutParams;
import static com.commeto.kuleuven.MP.support.Static.isNetworkAvailable;
import static com.commeto.kuleuven.MP.support.Static.makeToastLong;
import static com.commeto.kuleuven.MP.support.Static.tryLogin;

/**
 * Created by Jonas on 1/03/2018.
 */

public class CheckLoginActivity extends AppCompatActivity implements SSLResponseInterface{

//==================================================================================================
    //constants

    private String USERNAME;
//==================================================================================================
    //class specs

    private Context context;

    private SharedPreferences preferences;
//==================================================================================================
    //login interface

    private AsyncResponseInterface loginInterface = new AsyncResponseInterface() {
        @Override
        public void processFinished(HTTPResponse response) {

            if(response == null){
                makeToastLong(context, getString(R.string.login_unable));
            } else{

                //Only go to the BaseActivity if response code of the HTTP request is 200
                Intent intent = new Intent(CheckLoginActivity.this, response.getResponseCode() == 200
                        ? BaseActivity.class : LoginActivity.class
                );
                startActivity(intent);
                finish();
            }

            usedInterface = loginInterface;
        }
    };

    private AsyncResponseInterface usedInterface = loginInterface;
//==================================================================================================
    @Override
    public void onCreate(Bundle bundle) {

        //Necessary to initiate activity.
        super.onCreate(bundle);
        setContentView(R.layout.activity_check_login);

        //Setting constants.
        USERNAME = getString(R.string.preferences_username);

        //Setting attributes.
        context = getApplicationContext();
        preferences = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);

        //Setting the center logo
        (findViewById(R.id.logo)).setLayoutParams(getLayoutParams(context, R.drawable.logo));
        getWindow().setGravity(Gravity.CENTER_VERTICAL);
    }

    @Override
    public void onStart() {
        super.onStart();

        if(isNetworkAvailable(context)) goOnline();
        else goOffline();
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
    //private methods

    private void goOnline(){

        try {
            //Set certificate before attempting HTTP communication with server.
            setCertificate(this, this);
        } catch (Exception e){
            InternalIO.writeToLog(context, e);
        }
    }

    private void goOffline(){

        String username = preferences.getString(USERNAME, null);

        //Only allow online mode when username present in Shared¨Preferences
        if(username != null) {
            Intent intent = new Intent(this, BaseActivity.class);
            startActivityForResult(intent, 0);
            finish();
        } else {
            makeToastLong(context, getString(R.string.no_internet));
        }
    }

//==================================================================================================
    //Interface Override

    @Override
    public void onProcessFinished(boolean bool){

        if(bool) {

            String username = preferences.getString(USERNAME, null);
            String token = preferences.getString("token", null);

            if(username == null || token == null){

                Intent intent = new Intent(context, LoginActivity.class);
                startActivity(intent);
                finish();
            } else{
                tryLogin(context, usedInterface);
            }
        } else{
            makeToastLong(context, getString(R.string.error));
        }
    }
}
