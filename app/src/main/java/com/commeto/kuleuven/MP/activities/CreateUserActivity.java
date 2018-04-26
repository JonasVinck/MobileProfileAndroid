package com.commeto.kuleuven.MP.activities;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.commeto.kuleuven.MP.http.PostTask;
import com.commeto.kuleuven.MP.interfaces.AsyncResponseInterface;
import com.commeto.kuleuven.MP.dataClasses.HTTPResponse;
import com.commeto.kuleuven.MP.listeners.RoundedListener;
import com.commeto.kuleuven.MP.R;
import com.commeto.kuleuven.MP.support.InternalIO;

import org.json.JSONObject;

import java.util.HashMap;

import static com.commeto.kuleuven.MP.support.Static.makeToastLong;

/**
 * Created by Jonas on 1/03/2018.
 *
 * Activity started when a user has to be created.
 *
 * Uses:
 *  - PostTask
 */

public class CreateUserActivity extends AppCompatActivity implements AsyncResponseInterface{
//==================================================================================================
    //constants

    //Constants not in resources because only used in this activity.
    private final String USERNAME = "username";
    private final String PASSWORD = "password";
    private final String PASSWORD_CONFIRM = "password_confirm";
    private final String FIRST_NAME = "firstName";
    private final String LAST_NAME = "lastName";
    private final String EMAIL = "email";
    private String FULL_IP;
//==================================================================================================
    //class specs

    private Context context;

    private HashMap<String, EditText> editTextHashMap;

    private View.OnClickListener registerListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            register(view);
        }
    };
//==================================================================================================
    //lifecycle methods

    @Override
    public void onCreate(Bundle bundle) {

        //Necessary to initiate activity.
        super.onCreate(bundle);
        setContentView(R.layout.activity_create_user);

        //Setting constants.
        FULL_IP = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE)
                .getString(
                        getString(R.string.preferences_ip),
                        getString(R.string.hard_coded_ip)
                ) +
                getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE)
                .getString(getString(R.string.preferences_socket),
                        getString(R.string.hard_coded_socket)
                );

        //Setting attributes.
        context = getApplicationContext();
        findViewById(R.id.register).setOnTouchListener(new RoundedListener(context));
        editTextHashMap = new HashMap<>();
        editTextHashMap.put(USERNAME, (EditText) findViewById(R.id.username));
        editTextHashMap.put(PASSWORD, (EditText) findViewById(R.id.password));
        editTextHashMap.put(PASSWORD_CONFIRM, (EditText) findViewById(R.id.password_confirm));
        editTextHashMap.put(FIRST_NAME, (EditText) findViewById(R.id.name));
        editTextHashMap.put(LAST_NAME, (EditText) findViewById(R.id.last_name));
        editTextHashMap.put(EMAIL, (EditText) findViewById(R.id.email));
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
    //interface override

    /**
     * Sets different error messages depending on the response from the PostTask. Finishes the
     * Activity if the post was successful.
     *
     * @param response HTTP response from server.
     */

    public void processFinished(HTTPResponse response){

        if(response != null) {
            if (response.getResponseCode() == 200) {
                makeToastLong(context, "");
                finish();
            } else if(response.getResponseCode() == -1){
                error(getString(R.string.no_server));
            } else {
                ((EditText) findViewById(R.id.username)).setHighlightColor(getResources().getColor(R.color.red));
                error(getString(R.string.user_exists));
            }
        } else{
            error(getString(R.string.error));
        }
    }
//==================================================================================================
    //private methods

    /**
     * <pre>
     * - Clears fields containing passwords.
     * - Set error message TextView with given message and set visible.
     * - Re-enable the onClickListener that sneds the HTTP message to create a user?
     * - Set the background to have red borders.
     * </pre>
     * @param message error message needing to be displayed.
     */

    private void error(String message){

        editTextHashMap.get(PASSWORD).setText("");
        editTextHashMap.get(PASSWORD_CONFIRM).setText("");

        ((TextView) findViewById(R.id.error)).setText(message);
        findViewById(R.id.error).setVisibility(View.VISIBLE);

        findViewById(R.id.register).setOnClickListener(registerListener);
        findViewById(R.id.container).setBackgroundResource(R.drawable.rounded_error);
        ((View) findViewById(R.id.register).getParent()).setBackgroundResource(R.drawable.rounded_bottom_error);
    }

    /**
     * Clears any styling done by the error(String message) method.
     */

    private void clear(){

        findViewById(R.id.container).setBackgroundResource(R.drawable.rounded);
        ((View) findViewById(R.id.register).getParent()).setBackgroundResource(R.drawable.rounded_bottom_outer);
        findViewById(R.id.register).setOnClickListener(null);
    }

    /**
     * Changes given EditText's color to red if it's empty.
     *
     * @param editText EditText to change.
     */
    private void checkRed(EditText editText){
        if(editText.getText().toString().isEmpty()) {
            editText.getBackground().setColorFilter(
                    getResources().getColor(R.color.red),
                    PorterDuff.Mode.SRC_ATOP);
        }
    }
//==================================================================================================
    //button action

    /**
     * Used to check the correctness of the specified information. Will set error messages
     * accordingly if a specification isn't met.
     *
     * <pre>
     * checks if:
     *  - All fields are filled in.
     *  - Both passwords match.
     * </pre>
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void register(View view){

        clear();

        String username = editTextHashMap.get(USERNAME).getText().toString();
        String password = editTextHashMap.get(PASSWORD).getText().toString();
        String password_confirm = editTextHashMap.get(PASSWORD_CONFIRM).getText().toString();
        String firstname = editTextHashMap.get(FIRST_NAME).getText().toString();
        String lastname = editTextHashMap.get(LAST_NAME).getText().toString();
        String email = editTextHashMap.get(EMAIL).getText().toString();

        if(username.isEmpty() ||
                password.isEmpty() ||
                password_confirm.isEmpty() ||
                firstname.isEmpty() ||
                lastname.isEmpty() ||
                email.isEmpty()
                ){

            checkRed(editTextHashMap.get(USERNAME));
            checkRed(editTextHashMap.get(PASSWORD));
            checkRed(editTextHashMap.get(PASSWORD_CONFIRM));
            checkRed(editTextHashMap.get(FIRST_NAME));
            checkRed(editTextHashMap.get(LAST_NAME));
            checkRed(editTextHashMap.get(EMAIL));

            error(getString(R.string.fill_all));
        } else if(!password.equals(password_confirm)){
            checkRed(editTextHashMap.get(PASSWORD));
            checkRed(editTextHashMap.get(PASSWORD_CONFIRM));
            error(getString(R.string.password_mismatch));
        } else {

            try {
                JSONObject user = new JSONObject();
                user.put(USERNAME, username);
                user.put(PASSWORD, password);
                user.put(FIRST_NAME, firstname);
                user.put(LAST_NAME, lastname);
                user.put(EMAIL, email);
                user.put("group", "user");

                PostTask createUserTask = new PostTask(
                        FULL_IP,
                        getString(R.string.create_user),
                        this,
                        -1,
                        null
                );
                createUserTask.execute(
                        user.toString(),
                        "",
                        ""
                );
            } catch (Exception e){
                InternalIO.writeToLog(context, e);
            }
        }
    }
}
