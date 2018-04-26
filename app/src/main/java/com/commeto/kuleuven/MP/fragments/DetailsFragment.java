package com.commeto.kuleuven.MP.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.commeto.kuleuven.MP.R;

/**
 * <pre>
 * Created by Jonas on 12/04/2018.
 *
 * Fragment to display the different values of the ride information.
 * </pre>
 */

public class DetailsFragment extends Fragment{

    @Override
    public void onCreate(Bundle bundle){

        super.onCreate(bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_details, container, false);
    }
}
