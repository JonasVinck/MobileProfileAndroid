package com.commeto.kuleuven.MP.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;

import com.commeto.kuleuven.MP.dataClasses.HTTPResponse;
import com.commeto.kuleuven.MP.interfaces.AsyncResponseInterface;
import com.commeto.kuleuven.MP.interfaces.SSLResponseInterface;
import com.commeto.kuleuven.MP.R;
import com.commeto.kuleuven.MP.sqlSupport.LocalDatabase;
import com.commeto.kuleuven.MP.sqlSupport.LocalRoute;
import com.commeto.kuleuven.MP.sqlSupport.LocalRouteDAO;

import java.util.List;

import static com.commeto.kuleuven.MP.http.HTTPStatic.setCertificate;
import static com.commeto.kuleuven.MP.support.Static.getLayoutparams;
import static com.commeto.kuleuven.MP.support.Static.isNetworkAvailable;
import static com.commeto.kuleuven.MP.support.Static.makeToastLong;
import static com.commeto.kuleuven.MP.support.Static.tryLogin;

/**
 * Created by Jonas on 1/03/2018.
 */

public class CheckLoginActivity extends AppCompatActivity implements SSLResponseInterface{

//==================================================================================================
    //class specs

    private Context context;

    private SharedPreferences preferences;
    private String toWrite;

    private AlertDialog.Builder builder;
    private DialogInterface dialogInterface;
//==================================================================================================
    //login interface

    private AsyncResponseInterface blockingInterface = new AsyncResponseInterface() {
        @Override
        public void processFinished(HTTPResponse response) {
        }
    };

    private AsyncResponseInterface loginInterface = new AsyncResponseInterface() {
        @Override
        public void processFinished(HTTPResponse response) {

            if(response == null){
                makeToastLong(context, "Kan niet inloggen");
            } else{

                Intent intent = new Intent(CheckLoginActivity.this, response.getResponseCode() == 200
                        ? BaseActivity.class: LoginActivity.class
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
        super.onCreate(bundle);
        setContentView(R.layout.activity_check_login);
        context = getApplicationContext();
        preferences = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);

        (findViewById(R.id.logo)).setLayoutParams(getLayoutparams(context, R.drawable.logo));
        getWindow().setGravity(Gravity.CENTER_VERTICAL);

        if(preferences.getString("baseUrl", "").equals("")) {
            preferences.edit().putString("baseUrl", getString(R.string.hard_coded_ip)).apply();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        List<LocalRoute> routes = LocalDatabase.getInstance(context).localRouteDAO().debug();
        LocalRouteDAO dao = LocalDatabase.getInstance(context).localRouteDAO();

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
        if(dialogInterface != null) dialogInterface.dismiss();
    }
//==================================================================================================
    //private methods

    private void goOnline(){

        try {
            setCertificate(this, this);
        } catch (Exception e){
            makeToastLong(context, e.getMessage());
        }
    }

    private void goOffline(){

        String username = preferences.getString("username", null);
        String token = preferences.getString("token", null);

        if(token == null || username == null){

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("username", "offline");
            editor.apply();
        }

        preferences.edit().putString("offline", "1").apply();
        Intent intent = new Intent(this, BaseActivity.class);
        startActivityForResult(intent, 0);
        finish();
    }

//==================================================================================================
    //Interface Override

    @Override
    public void onProcessFinished(boolean bool){

        if(bool) {
;
            String username = preferences.getString("username", null);
            String token = preferences.getString("token", null);

            if(username == null || token == null){

                Intent intent = new Intent(context, LoginActivity.class);
                startActivity(intent);
                finish();
            } else{
                tryLogin(context, usedInterface);
                usedInterface = blockingInterface;
            }
        } else{
            makeToastLong(context, getString(R.string.ssl_error));
        }
    }
}
