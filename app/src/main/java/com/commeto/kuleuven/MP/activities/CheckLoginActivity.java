package com.commeto.kuleuven.MP.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;

import com.commeto.kuleuven.MP.dataClasses.HTTPResponse;
import com.commeto.kuleuven.MP.interfaces.AsyncResponseInterface;
import com.commeto.kuleuven.MP.R;
import com.commeto.kuleuven.MP.support.InternalIO;

import static com.commeto.kuleuven.MP.http.HTTPStatic.setCertificate;
import static com.commeto.kuleuven.MP.support.Static.getLayoutParams;
import static com.commeto.kuleuven.MP.support.Static.isNetworkAvailable;
import static com.commeto.kuleuven.MP.support.Static.makeToastLong;
import static com.commeto.kuleuven.MP.support.Static.tryLogin;

/**
 * Created by Jonas on 1/03/2018.
 * <p>
 * First Activity that opens when the app is started. Will first check for an internet connection,
 * if no connection is available if will check if there is a username in the SharedPreferences. If
 * a username is found, it means a user was logged in. The Activity will then route the app to the
 * BaseActivity ans display a message that it is running in offline mode.
 * </p>
 * <pre>
 * Uses:
 *  - GetTask
 * </pre>
 */

public class CheckLoginActivity extends AppCompatActivity{

//==================================================================================================
    //constants

    private String USERNAME;
//==================================================================================================
    //class specs

    private Context context;

    private SharedPreferences preferences;
//==================================================================================================
    //login interface

    /**
     * Interface used to handle the result of the login message.
     */
    private AsyncResponseInterface loginInterface = new AsyncResponseInterface() {
        @Override
        public void processFinished(HTTPResponse response) {

            if (response == null) {
                makeToastLong(context, getString(R.string.login_unable));
            } else {

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

        if (isNetworkAvailable(context)) goOnline();
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

    /**
     * Method called when network is available. First the certificate will be added to the
     * TrustStore. The interface used to handle this result will decide the next step.
     */
    private void goOnline() {

        try {
            //Set certificate before attempting HTTP communication with server.
            setCertificate(this);

            String username = preferences.getString(USERNAME, null);
            String token = preferences.getString(getString(R.string.preferences_token), null);

            if (username == null || token == null) {

                Intent intent = new Intent(context, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                tryLogin(context, usedInterface);
            }
        } catch (Exception e) {
            InternalIO.writeToLog(context, e);
        }
    }

    /**
     * Called when no network connection is found. If there is still a username in the
     * SharedPreferences, offline mode will be started and the app will route to the BaseActivity.
     * The BaseActivity checks the login every time the onStart is called to keep checking for the
     * validity of the token.
     */
    private void goOffline() {

        String username = preferences.getString(USERNAME, null);

        //Only allow online mode when username present in Shared¨Preferences
        if (username != null) {
            Intent intent = new Intent(this, BaseActivity.class);
            startActivityForResult(intent, 0);
            finish();
        } else {
            makeToastLong(context, getString(R.string.no_internet));
        }
    }
}
