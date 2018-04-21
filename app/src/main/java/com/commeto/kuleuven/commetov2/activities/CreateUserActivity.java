package com.commeto.kuleuven.commetov2.activities;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.commeto.kuleuven.commetov2.interfaces.AsyncResponseInterface;
import com.commeto.kuleuven.commetov2.http.CreateUserTask;
import com.commeto.kuleuven.commetov2.dataClasses.HTTPResponse;
import com.commeto.kuleuven.commetov2.listeners.RoundedListener;
import com.commeto.kuleuven.commetov2.R;
import com.commeto.kuleuven.commetov2.support.InternalIO;

import org.json.JSONObject;

import java.util.HashMap;

import static com.commeto.kuleuven.commetov2.support.Static.makeToastLong;

/**
 * Created by Jonas on 1/03/2018.
 */

public class CreateUserActivity extends AppCompatActivity implements AsyncResponseInterface{
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
        super.onCreate(bundle);
        setContentView(R.layout.activity_create_user);
        context = getApplicationContext();
        findViewById(R.id.register).setOnTouchListener(new RoundedListener(context));
        editTextHashMap = new HashMap<>();
        editTextHashMap.put("username", (EditText) findViewById(R.id.username));
        editTextHashMap.put("password", (EditText) findViewById(R.id.password));
        editTextHashMap.put("password_confirm", (EditText) findViewById(R.id.password_confirm));
        editTextHashMap.put("first_name", (EditText) findViewById(R.id.name));
        editTextHashMap.put("last_name", (EditText) findViewById(R.id.last_name));
        editTextHashMap.put("email", (EditText) findViewById(R.id.email));
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

    public void processFinished(HTTPResponse response){

        if(response != null) {
            if (response.getResponseCode() == 200 && response.getResponsBody().equals("Done")) {
                makeToastLong(context, "");
                finish();
            } else if(response.getResponseCode() == -1){
                makeToastLong(context, getString(R.string.no_server));
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

    private void error(String message){

        ((EditText) findViewById(R.id.password)).setText("");
        ((EditText) findViewById(R.id.password_confirm)).setText("");

        ((TextView) findViewById(R.id.error)).setText(message);
        findViewById(R.id.error).setVisibility(View.VISIBLE);
        findViewById(R.id.container).setBackgroundResource(R.drawable.rounded_error);
        findViewById(R.id.register).setOnClickListener(registerListener);
        ((View) findViewById(R.id.register).getParent()).setBackgroundResource(R.drawable.rounded_bottom_error);
    }

    private void clear(){

        findViewById(R.id.container).setBackgroundResource(R.drawable.rounded);
        ((View) findViewById(R.id.register).getParent()).setBackgroundResource(R.drawable.rounded_bottom_outer);
        findViewById(R.id.register).setOnClickListener(null);
    }

    private void makeRed(int id){
        findViewById(id).getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
    }
//==================================================================================================
    //button action

    public void register(View view){

        clear();

        String username = editTextHashMap.get("username").getText().toString();
        String password = editTextHashMap.get("password").getText().toString();
        String password_confirm = editTextHashMap.get("password_confirm").getText().toString();
        String firstname = editTextHashMap.get("first_name").getText().toString();
        String lastname = editTextHashMap.get("last_name").getText().toString();
        String email = editTextHashMap.get("email").getText().toString();

        if(username.equals("") ||
                password.equals("") ||
                password_confirm.equals("") ||
                firstname.equals("") ||
                lastname.equals("") ||
                email.equals("")
                ){

            if(username.equals("")) makeRed(R.id.username);
            if(password.equals("")) makeRed(R.id.password);
            if(password_confirm.equals("")) makeRed(R.id.password_confirm);
            if(firstname.equals("")) makeRed(R.id.name);
            if(lastname.equals("")) makeRed(R.id.last_name);
            if(email.equals("")) makeRed(R.id.email);

            error(getString(R.string.fill_all));
        } else if(!password.equals(password_confirm)){
            makeRed(R.id.password);
            makeRed(R.id.password_confirm);
            error(getString(R.string.password_mismatch));
        } else {

            try {
                JSONObject user = new JSONObject();
                user.put("username", username);
                user.put("password", password);
                user.put("firstName", firstname);
                user.put("lastName", lastname);
                user.put("email", email);
                user.put("group", "user");

                CreateUserTask createUserTask = new CreateUserTask(
                        getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE).getString("baseUrl", getString(R.string.hard_coded_ip)),
                        "/MP/service/user/create",
                        this,
                        -1
                );
                createUserTask.execute(user);
            } catch (Exception e){
                InternalIO.writeToLog(context, e);
            }
        }
    }
}
