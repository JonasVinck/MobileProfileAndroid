package com.commeto.kuleuven.commetov2.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.commeto.kuleuven.commetov2.R;

/**
 * Created by Jonas on 15/03/2018.
 */

public class MusicControlFragment extends Fragment{

    @Override
    public void onCreate(Bundle bundle){

        super.onCreate(bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_media_controls, container, false);
    }
}
