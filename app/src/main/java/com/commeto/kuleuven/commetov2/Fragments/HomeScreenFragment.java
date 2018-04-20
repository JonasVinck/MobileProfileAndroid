package com.commeto.kuleuven.commetov2.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.commeto.kuleuven.commetov2.Activities.Callibration;
import com.commeto.kuleuven.commetov2.Activities.LoginActivity;
import com.commeto.kuleuven.commetov2.Activities.MeasuringActivity;
import com.commeto.kuleuven.commetov2.DataClasses.HTTPResponse;
import com.commeto.kuleuven.commetov2.Interfaces.AsyncResponseInterface;
import com.commeto.kuleuven.commetov2.Listeners.UnderlineButtonListener;
import com.commeto.kuleuven.commetov2.R;
import com.commeto.kuleuven.commetov2.SQLSupport.LocalDatabase;
import com.commeto.kuleuven.commetov2.SQLSupport.LocalRoute;
import com.commeto.kuleuven.commetov2.Support.InternalIO;


import static com.commeto.kuleuven.commetov2.Support.Static.makeToastLong;
import static com.commeto.kuleuven.commetov2.Support.Static.tryLogin;

/**
 * Created by Jonas on 13/04/2018.
 */

public class HomeScreenFragment extends Fragment {

//==================================================================================================
    //class specs

    private Context context;

    private SharedPreferences preferences;
//==================================================================================================
    //lifecycle methods

    public static HomeScreenFragment newInstance() {
        HomeScreenFragment fragment = new HomeScreenFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        context = getActivity();
        preferences = getActivity().getSharedPreferences("commeto", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_screen, container, false);

        view.findViewById(R.id.start).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    ((ImageView) view.findViewById(R.id.start_button)).setImageDrawable(
                            getResources().getDrawable(R.drawable.logo_svg_clicked)
                    );
                }

                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    ((ImageView) view.findViewById(R.id.start_button)).setImageDrawable(
                            getResources().getDrawable(R.drawable.logo_svg)
                    );
                }

                return false;
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        //TODO sundown

        try {
            if (preferences.getInt("calibration", 0) == 0) {
                Intent intent = new Intent(getActivity(), Callibration.class);
                startActivity(intent);
            }
        } catch (Exception e){

            if (preferences.getFloat("calibration", 0) == 0) {
                Intent intent = new Intent(getActivity(), Callibration.class);
                startActivity(intent);
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
}
