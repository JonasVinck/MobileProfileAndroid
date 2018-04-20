package com.commeto.kuleuven.commetov2.Activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;

import com.commeto.kuleuven.commetov2.DataClasses.HTTPResponse;
import com.commeto.kuleuven.commetov2.Interfaces.AsyncResponseInterface;
import com.commeto.kuleuven.commetov2.HTTP.GetTask;
import com.commeto.kuleuven.commetov2.Interfaces.SSLResponseInterface;
import com.commeto.kuleuven.commetov2.R;
import com.commeto.kuleuven.commetov2.SQLSupport.LocalDatabase;
import com.commeto.kuleuven.commetov2.SQLSupport.LocalRoute;
import com.commeto.kuleuven.commetov2.SQLSupport.LocalRouteDAO;

import java.io.FileOutputStream;
import java.util.List;

import static com.commeto.kuleuven.commetov2.HTTP.HTTPStatic.setCertificate;
import static com.commeto.kuleuven.commetov2.Support.Static.getLayoutparams;
import static com.commeto.kuleuven.commetov2.Support.Static.isNetworkAvailable;
import static com.commeto.kuleuven.commetov2.Support.Static.makeToastLong;
import static com.commeto.kuleuven.commetov2.Support.Static.tryLogin;

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
        preferences = getSharedPreferences("commeto", MODE_PRIVATE);

        (findViewById(R.id.logo)).setLayoutParams(getLayoutparams(context, R.drawable.logo));
        getWindow().setGravity(Gravity.CENTER_VERTICAL);

        if(preferences.getString("baseUrl", "").equals("")) {
            preferences.edit().putString("baseUrl", "213.118.13.252").apply();
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
            makeToastLong(context, "an error occured with the certificate");
        }
    }
//==================================================================================================
    //debug

    private void writeDb(){
        List<LocalRoute> localRoutes = LocalDatabase.getInstance(getApplicationContext()).localRouteDAO().debug();
        StringBuilder builder = new StringBuilder();
        for(LocalRoute route: localRoutes){
            builder.append(route.toString());
            builder.append("\n=============================\n");
        }
        toWrite = builder.toString();
        createFile("text/plain", "test.txt");
    }

    @TargetApi(19)
    private void createFile(String mimeType, String fileName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        intent.addCategory(Intent.CATEGORY_OPENABLE);

        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        startActivityForResult(intent, 43); //43 = write request code
    }

    private void alterDocument(Uri uri) {

        try {
            ParcelFileDescriptor pfd = getApplicationContext().getContentResolver().
                    openFileDescriptor(uri, "w");
            FileOutputStream fileOutputStream =
                    new FileOutputStream(pfd.getFileDescriptor());
            fileOutputStream.write(toWrite.getBytes());
            fileOutputStream.close();
            pfd.close();
        } catch (Exception e) {
            makeToastLong(context, "er ging iets fout bij het opslaan");
        }
    }

    @Override
    public void onActivityResult(int requestcode, int resultcode, Intent resultData){

        if((requestcode == 44 || requestcode == 43 ) && resultcode == Activity.RESULT_OK){
            if(resultData != null){
                alterDocument(resultData.getData());
            }
        }
    }
}
