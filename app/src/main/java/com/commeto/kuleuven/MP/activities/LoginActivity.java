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
 *
 * Activity used to get a new token from the server to identify the user.
 *
 * Uses:
 *  - GetTask
 */

public class LoginActivity extends AppCompatActivity {
//==================================================================================================
    //class specs

    private Context context;

    private String username;

    private TextView createLink;
    private LinearLayout login;

    /**
     * onClickListener for the login button.
     */
    private View.OnClickListener loginListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            clear();
            login(view);
        }
    };

    /**
     * onClickListener for the create new user button.
     */
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

    /**
     * Interface object to get result of login attempt.
     */
    AsyncResponseInterface loginResult = new AsyncResponseInterface() {
        @Override
        public void processFinished(HTTPResponse response) {
            loginAttempt(response);
        }
    };
//==================================================================================================
    //private methods

    /**
     * Method to send a login message.
     *
     * @param username The username used to log in.
     * @param password The associated password.
     */
    private void login(String username, String password){

        findViewById(R.id.error).setVisibility(View.GONE);

        login.setOnClickListener(null);
        createLink.setOnClickListener(null);

        try {
            SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
            GetTask loginTask = new GetTask(
                    preferences.getString("baseUrl", getString(R.string.hard_coded_ip)) + ":" + preferences.getString("socket", getString(R.string.hard_coded_socket)),
                    "/MP/service/login",
                    loginResult,
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

    /**
     * <pre>
     * Method that checks the result of the login request.
     *
     * possible responses:
     *  - null  No response gotten.
     *  - 200   Login successful, token in response's body.
     *  - -1    The server could not be accessed.
     * </pre>
     * @param response The response gotten from the server for the login message.
     */
    private void loginAttempt(HTTPResponse response){
        if(response != null) {
            if (response.getResponseCode() == 200) {

                setResult(RESULT_OK);
                Intent intent = new Intent(context, BaseActivity.class);

                //Add the username and the token to the SharedPreferences, user is now logged in.
                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", username);
                editor.putString("token", response.getResponseBody());
                editor.apply();

                //restart the BaseActivity if necessary.
                if(getIntent().getBooleanExtra("home", true)) {
                    startActivity(intent);
                }
                finish();
            } else if(response.getResponseCode() == -1){
                //TODO clean
                makeToastLong(context, "Server mogelijk niet beschikbaar.");

                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", username);
                editor.putString("token", response.getResponseBody());
                editor.apply();

                startActivity(new Intent(context, BaseActivity.class)
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

    /**
     * Method to set the content of the error message field to a certain error message. View id:
     * error. Also styles the container to have a red border.
     *
     * @param message Message to be displayed.
     */
    private void error(String message){
        login.setOnClickListener(loginListener);
        createLink.setOnClickListener(createListener);
        findViewById(R.id.container).setBackgroundResource(R.drawable.rounded_error);
        ((TextView) findViewById(R.id.error)).setText(message);
        findViewById(R.id.error).setVisibility(View.VISIBLE);
        ((View) findViewById(R.id.login).getParent()).setBackgroundResource(R.drawable.rounded_bottom_error);
    }

    /**
     * Removes any styling done by the error(String message) function.
     */
    private void clear(){
        login.setOnClickListener(null);
        createLink.setOnClickListener(null);
        findViewById(R.id.container).setBackgroundResource(R.drawable.rounded);
        findViewById(R.id.error).setVisibility(View.GONE);
        ((View) findViewById(R.id.login).getParent()).setBackgroundResource(R.drawable.rounded_bottom_outer);
    }

    /**
     * <pre>
     * Used to start the login. Checks the validity of all the fields before starting.
     *
     * Checks if:
     *  - password is filled in.
     *  - username is filled in.
     * </pre>
     * @param view
     */
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
