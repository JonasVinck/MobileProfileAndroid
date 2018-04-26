package com.commeto.kuleuven.MP.fragments;

import android.app.Activity;
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
 * <pre>
 * Created by Jonas on 13/04/2018.
 *
 * Fragment to display settings.
 * </pre>
 */

public class SettingsFragment extends Fragment{
//==================================================================================================
    //constants

    private String IP;
    private String SOCKET;
    private String EXPORT_FULL;
    private String AUTO_UPLOAD;
    private String AUTO_SYNC;
    private String DEBUG;
    private String CALIBRATION;
//==================================================================================================
    //class specs

    private Activity activity;

    private LayoutUpdateInterface updateInterface = new LayoutUpdateInterface() {
        @Override
        public void update() {
            resetSettings();
        }
    };

//==================================================================================================
    //life cycle

    /**
     * Get a new instance of the SettingsFragment.
     *
     * @return A new instance of the SettingsFragment.
     */
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

        //Get used constants from resources.
        setConstants();
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
                    getString(R.string.no_version)
            );
        }

        final SharedPreferences preferences = activity.getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);

        ((Switch) view.findViewById(R.id.export_full_switch)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                preferences.edit()
                        .putBoolean(getString(R.string.preferences_export_full), b)
                        .apply();
            }
        });

        ((Switch) view.findViewById(R.id.auto_upload_option)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                preferences.edit()
                        .putBoolean(getString(R.string.preferences_auto_upload), b)
                        .apply();
            }
        });

        ((Switch) view.findViewById(R.id.auto_sync_option)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                preferences.edit()
                        .putBoolean(getString(R.string.preferences_auto_sync), b)
                        .apply();
            }
        });

        ((Switch) view.findViewById(R.id.debug_switch)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                preferences.edit()
                        .putBoolean(getString(R.string.preferences_debug), b)
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

    /**
     * Resets the setting so that it matches the SharedPreferences.
     */
    private void resetSettings(){

        SharedPreferences preferences = activity.getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        ((TextView) activity.findViewById(R.id.ip)).setText(preferences.getString(IP, getString(R.string.hard_coded_ip)));
        ((TextView) activity.findViewById(R.id.socket)).setText(preferences.getString(SOCKET, getString(R.string.hard_coded_socket)));

        ((Switch) activity.findViewById(R.id.export_full_switch)).setChecked(
                preferences.getBoolean(EXPORT_FULL, false)
        );

        ((Switch) activity.findViewById(R.id.auto_upload_option)).setChecked(
                preferences.getBoolean(AUTO_UPLOAD, true)
        );

        ((Switch) activity.findViewById(R.id.auto_sync_option)).setChecked(
                preferences.getBoolean(AUTO_SYNC, true)
        );

        ((Switch) activity.findViewById(R.id.debug_switch)).setChecked(
                preferences.getBoolean(DEBUG, true)
        );

        activity.findViewById(R.id.debug_container).setVisibility(preferences.getBoolean(DEBUG, true) ?
                View.VISIBLE : View.GONE
        );

        boolean test = preferences.getInt(CALIBRATION, 0) > 45;
        activity.findViewById(R.id.can_measure).setVisibility(test ? View.VISIBLE : View.GONE);
        activity.findViewById(R.id.can_not_measure).setVisibility(test ? View.GONE : View.VISIBLE);
    }

    /**
     * Resets all the debug options in the SharedPreferences.
     */
    private void resetDebug(){

        SharedPreferences preferences = activity.getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        if(!preferences.getBoolean(DEBUG, false)){
            preferences.edit()
                    .putBoolean(EXPORT_FULL, false)
                    .putBoolean(AUTO_UPLOAD, false)
                    .putString(IP, getString(R.string.hard_coded_ip))
                    .putString(SOCKET, getString(R.string.hard_coded_socket))
                    .apply();
        }
    }

    /**
     * Get the needed constants from the resource files.
     */
    private void setConstants(){
        IP = getString(R.string.preferences_ip);
        SOCKET = getString(R.string.preferences_socket);
        EXPORT_FULL = getString(R.string.preferences_export_full);
        AUTO_UPLOAD = getString(R.string.preferences_auto_upload);
        AUTO_SYNC = getString(R.string.preferences_auto_sync);
        DEBUG = getString(R.string.preferences_debug);
        CALIBRATION = getString(R.string.preferences_calibration);
    }
//==================================================================================================
    //get interface

    public LayoutUpdateInterface getUpdateInterface(){
        return updateInterface;
    }
}
