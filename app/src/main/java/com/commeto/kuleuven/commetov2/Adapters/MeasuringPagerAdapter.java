package com.commeto.kuleuven.commetov2.Adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.commeto.kuleuven.commetov2.Fragments.MeasuringGraphFragment;
import com.commeto.kuleuven.commetov2.Fragments.MeasuringMapFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Jonas on 10/04/2018.
 */

public class MeasuringPagerAdapter extends FragmentPagerAdapter {

    public final int PAGE_COUNT;

    private ArrayList<Fragment> fragments;

    public MeasuringPagerAdapter(FragmentManager fragmentManager, List<Fragment> fragments) {
        super(fragmentManager);

        PAGE_COUNT = fragments.size();
        this.fragments = new ArrayList<>();
        this.fragments.addAll(fragments);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title;
        try {
            title = ((MeasuringMapFragment) fragments.get(position)).getTitle();
        } catch (ClassCastException e){
            title = ((MeasuringGraphFragment) fragments.get(position)).getTitle();
        }

        if(title == null) title = (position == 0 ? "Veeg voor meer opties" : "");
        return title;
    }

    public void append(double lat, double lon, double bearing, double altitude, double vibration, double lightLevel){
        for(Fragment fragment: fragments){
            try{
                ((MeasuringMapFragment) fragment).append(lat, lon, bearing);
            } catch (ClassCastException e){
                MeasuringGraphFragment graphFragment = (MeasuringGraphFragment) fragment;
                switch (graphFragment.getTitle()){
                    case "Trilling":
                        graphFragment.append(vibration);
                        break;
                    case "Verlichting":
                        graphFragment.append(lightLevel);
                        break;
                    case "Hoogtverschil":
                        graphFragment.append(altitude);
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
