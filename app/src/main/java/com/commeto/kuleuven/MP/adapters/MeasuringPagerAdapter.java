package com.commeto.kuleuven.MP.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.commeto.kuleuven.MP.fragments.MeasuringGraphFragment;
import com.commeto.kuleuven.MP.fragments.MeasuringMapFragment;

import java.util.HashMap;

import static com.commeto.kuleuven.MP.support.Static.makeToastShort;

/**
 * Created by Jonas on 10/04/2018.
 */

public class MeasuringPagerAdapter extends FragmentPagerAdapter {

    private final int PAGE_COUNT;

    private HashMap<String, Fragment> fragments;
    private String[] titles;

    public MeasuringPagerAdapter(FragmentManager fragmentManager, HashMap<String, Fragment> fragments, String[] titles) {
        super(fragmentManager);

        PAGE_COUNT = fragments.size();
        this.fragments = fragments;
        this.titles = titles;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(titles[position]);
    }

    @Override
    public CharSequence getPageTitle(int position) {

        return titles[position];
    }

    public void append(double lat, double lon, double altitude, double vibration, double lightLevel){

        if(fragments.containsKey("Trilling")) ((MeasuringGraphFragment) fragments.get("Trilling")).append(vibration);
        ((MeasuringGraphFragment) fragments.get("Verlichting")).append(lightLevel);
        ((MeasuringGraphFragment) fragments.get("Hoogteverschil")).append(altitude);
        ((MeasuringMapFragment) fragments.get("Kaart")).append(lat, lon);
    }
}
