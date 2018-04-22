package com.commeto.kuleuven.MP.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.commeto.kuleuven.MP.interfaces.AsyncResponseInterface;
import com.commeto.kuleuven.MP.dataClasses.HTTPResponse;
import com.commeto.kuleuven.MP.http.GetTask;
import com.commeto.kuleuven.MP.interfaces.SSLResponseInterface;
import com.commeto.kuleuven.MP.listeners.RoundedListener;
import com.commeto.kuleuven.MP.R;
import com.commeto.kuleuven.MP.support.InternalIO;

import static com.commeto.kuleuven.MP.http.HTTPStatic.setCertificate;
import static com.commeto.kuleuven.MP.support.Static.makeToastLong;

/**
 * Created by Jonas on 1/03/2018.
 */

public class LoginActivity extends AppCompatActivity implements AsyncResponseInterface, SSLResponseInterface {
//==================================================================================================
    //class specs

    private Context context;

    private String username;

    private TextView createLink;
    private LinearLayout login;

    private View.OnClickListener loginListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            clear();
            login(view);
        }
    };

    private View.OnClickListener createListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Intent intent = new Intent(getApplicationContext(), CreateUserActivity.class);
            startActivity(intent);
        }
    };
//==================================================================================================
    //lifecycle methods

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_login);
        context = getApplicationContext();

        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        ((TextView) findViewById(R.id.username)).setText(preferences.getString("username", ""));

        createLink = findViewById(R.id.register_link);
        createLink.setOnClickListener(createListener);

        findViewById(R.id.login).setOnTouchListener(new RoundedListener(context));
        login = findViewById(R.id.login);
    }

    @Override
    public void onStart() {
        super.onStart();

        if(getIntent().getBooleanExtra("certificate", false)){
            try {
                setCertificate(this, this);
            } catch (Exception e){
                InternalIO.writeToLog(context, e);
            }
        }
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
    //interface overrides

    @Override
    public void onProcessFinished(boolean bool){
    }

    @Override
    public void processFinished(HTTPResponse response){

        if(response != null) {
            if (response.getResponseCode() == 200) {

                setResult(RESULT_OK);
                Intent intent = new Intent(context, BaseActivity.class);

                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("offline", false);
                editor.putString("username", username);
                editor.putString("token", response.getResponsBody());
                editor.apply();

                if(getIntent().getBooleanExtra("home", true)) {
                    startActivity(intent);
                }
                finish();
            } else if(response.getResponseCode() == -1){
                makeToastLong(context, "Server mogelijk niet beschikbaar.");

                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("offline", true);
                editor.putString("username", username);
                editor.putString("token", response.getResponsBody());
                editor.apply();

                startActivity(new Intent(context, BaseActivity.class)
                        .putExtra("offline", true)
                );
                finish();
            }
            else{
                ((EditText) findViewById(R.id.password)).setHighlightColor(getResources().getColor(R.color.red));
                error(getString(R.string.wrong_password));
            }
        } else{
            makeToastLong(context, "Er ging iets mis.");
        }
    }
//==================================================================================================
    //private methods

    private void login(String username, String password){

        findViewById(R.id.error).setVisibility(View.GONE);

        login.setOnClickListener(null);
        createLink.setOnClickListener(null);

        try {
            SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
            GetTask loginTask = new GetTask(
                    preferences.getString("baseUrl", getString(R.string.hard_coded_ip)) + ":" + preferences.getString("socket", getString(R.string.hard_coded_socket)),
                    "/MP/service/login",
                    this,
                    null,
                    -1
            );
            String[] pass = new String[]{
                    username,
                    password
            };
            loginTask.execute(pass);
        } catch (Exception e) {
            InternalIO.writeToLog(context, e);

            login.setOnClickListener(loginListener);
            createLink.setOnClickListener(createListener);
        }
    }

    private void error(String message){
        login.setOnClickListener(loginListener);
        createLink.setOnClickListener(createListener);
        findViewById(R.id.container).setBackgroundResource(R.drawable.rounded_error);
        ((TextView) findViewById(R.id.error)).setText(message);
        findViewById(R.id.error).setVisibility(View.VISIBLE);
        ((View) findViewById(R.id.login).getParent()).setBackgroundResource(R.drawable.rounded_bottom_error);
    }

    private void clear(){
        login.setOnClickListener(null);
        createLink.setOnClickListener(null);
        findViewById(R.id.container).setBackgroundResource(R.drawable.rounded);
        findViewById(R.id.error).setVisibility(View.GONE);
        ((View) findViewById(R.id.login).getParent()).setBackgroundResource(R.drawable.rounded_bottom_outer);
    }
    public void login(View view){

        username = ((TextView) findViewById(R.id.username)).getText().toString();
        String password = ((TextView) findViewById(R.id.password)).getText().toString();

        if(username.equals("")){
            ((EditText) findViewById(R.id.username)).getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
            error(getString(R.string.list_username));
        } else if(password.equals("")){
            ((EditText) findViewById(R.id.password)).getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
            error(getString(R.string.list_password));
        } else{
            login(username, password);
        }
    }
}
