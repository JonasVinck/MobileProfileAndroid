package com.commeto.kuleuven.MP.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.commeto.kuleuven.MP.R;

/**
 * Created by Jonas on 15/03/2018.
 *
 * Fragment to add music controls to layout. Actions specified in activity. Only used to recycle
 * layout.
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
