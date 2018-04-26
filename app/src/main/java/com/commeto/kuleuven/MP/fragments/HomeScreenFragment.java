package com.commeto.kuleuven.MP.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.commeto.kuleuven.MP.activities.Callibration;
import com.commeto.kuleuven.MP.R;

/**
 * <pre>
 * Created by Jonas on 13/04/2018.
 *
 * Fragment to display the starting button.
 * </pre>
 */

public class HomeScreenFragment extends Fragment {

//==================================================================================================
    //class specs

    private Context context;

    private SharedPreferences preferences;
//==================================================================================================
    //lifecycle methods

    /**
     * Gets a new instance of the HomeScreenFragment.
     *
     * @return A new HomecreenFragment.
     */
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
        preferences = getActivity().getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_screen, container, false);

        //Changing the logo color onTouch.
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
