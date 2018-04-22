package com.commeto.kuleuven.MP.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.commeto.kuleuven.MP.interfaces.LayoutUpdateInterface;
import com.commeto.kuleuven.MP.R;

import org.jetbrains.annotations.NotNull;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Jonas on 13/04/2018.
 *
 * Fragment to display settings.
 */

public class SettingsFragment extends Fragment{

    private Context context;
    private Activity activity;

    private LayoutUpdateInterface updateInterface = new LayoutUpdateInterface() {
        @Override
        public void update() {
            resetSettings();
        }
    };

//==================================================================================================
    //life cycle

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        activity = getActivity();
        context = getActivity();
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_settings, container, false);

        try {
            ((TextView) view.findViewById(R.id.version)).setText(
                    activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName
            );
        } catch (PackageManager.NameNotFoundException e){
            ((TextView) view.findViewById(R.id.version)).setText(
                    "geen versie gevonden..."
            );
        }

        final SharedPreferences preferences = activity.getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);

        ((Switch) view.findViewById(R.id.export_full_switch)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                preferences.edit()
                        .putBoolean("export_full", b)
                        .apply();
            }
        });

        ((Switch) view.findViewById(R.id.auto_upload_option)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                preferences.edit()
                        .putBoolean("auto_upload", b)
                        .apply();
            }
        });

        ((Switch) view.findViewById(R.id.auto_sync_option)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                preferences.edit()
                        .putBoolean("auto_sync", b)
                        .apply();
            }
        });

        ((Switch) view.findViewById(R.id.debug_switch)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                preferences.edit()
                        .putBoolean("debug", b)
                        .apply();

                view.findViewById(R.id.debug_container).setVisibility(b ? View.VISIBLE : View.GONE);
            }
        });
        
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();

        resetSettings();
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    public void onDestroy(){

        resetDebug();

        super.onDestroy();
    }
//==================================================================================================
    //private functions

    private void resetSettings(){

        SharedPreferences preferences = activity.getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        ((TextView) activity.findViewById(R.id.ip)).setText(preferences.getString("baseUrl", "no ip available"));
        ((TextView) activity.findViewById(R.id.socket)).setText(preferences.getString("socket", getString(R.string.hard_coded_socket)));

        ((Switch) activity.findViewById(R.id.export_full_switch)).setChecked(
                preferences.getBoolean("export_full", false)
        );

        ((Switch) activity.findViewById(R.id.auto_upload_option)).setChecked(
                preferences.getBoolean("auto_upload", true)
        );

        ((Switch) activity.findViewById(R.id.auto_sync_option)).setChecked(
                preferences.getBoolean("auto_sync", true)
        );

        ((Switch) activity.findViewById(R.id.debug_switch)).setChecked(
                preferences.getBoolean("debug", true)
        );

        activity.findViewById(R.id.debug_container).setVisibility(preferences.getBoolean("debug", true) ?
                View.VISIBLE : View.GONE
        );

        boolean test = preferences.getInt("calibration", 0) > 45;
        activity.findViewById(R.id.can_measure).setVisibility(test ? View.VISIBLE : View.GONE);
        activity.findViewById(R.id.can_not_measure).setVisibility(test ? View.GONE : View.VISIBLE);
    }

    private void resetDebug(){

        SharedPreferences preferences = activity.getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        if(!preferences.getBoolean("debug", false)){
            preferences.edit()
                    .putBoolean("export_full", false)
                    .putBoolean("auto_upload", false)
                    .putString("baseUrl", getString(R.string.hard_coded_ip))
                    .apply();
        }
    }
//==================================================================================================
    //get interface

    public LayoutUpdateInterface getUpdateInterface(){
        return updateInterface;
    }
}
