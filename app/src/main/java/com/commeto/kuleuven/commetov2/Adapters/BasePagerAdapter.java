package com.commeto.kuleuven.commetov2.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.MotionEvent;

import com.commeto.kuleuven.commetov2.Fragments.GlobalMapFragment;
import com.commeto.kuleuven.commetov2.Fragments.HomeScreenFragment;
import com.commeto.kuleuven.commetov2.Fragments.RouteListFragment;
import com.commeto.kuleuven.commetov2.Fragments.SettingsFragment;
import com.commeto.kuleuven.commetov2.Fragments.StatsFragment;
import com.commeto.kuleuven.commetov2.Interfaces.RouteListInterface;

import javax.microedition.khronos.opengles.GL;

/**
 * Created by Jonas on 13/04/2018.
 */

public class BasePagerAdapter extends FragmentPagerAdapter {

    private final int PAGE_COUNT = 5;

    private HomeScreenFragment homeScreenFragment;
    private StatsFragment statsFragment;
    private RouteListFragment listFragment;
    private SettingsFragment settingsFragment;
    private GlobalMapFragment globalMapFragment;

    public BasePagerAdapter(FragmentManager manager){

        super(manager);
        homeScreenFragment = HomeScreenFragment.newInstance();
        statsFragment = StatsFragment.newInstance();
        listFragment = RouteListFragment.newInstance();
        settingsFragment = SettingsFragment.newInstance();
        globalMapFragment = GlobalMapFragment.newInstance();

    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return globalMapFragment;
            case 1:
                return statsFragment;
            case 3:
                return listFragment;
            case 4:
                return settingsFragment;
            default:
                return homeScreenFragment;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Global map";
            case 1:
                return "List";
            case 3:
                return "Stats";
            case 4:
                return "Settings";
            default:
                return "Homescreen";
        }
    }
//==================================================================================================
    //update layout

    public void updateStatsLayout(){
        if(statsFragment != null) statsFragment.getUpdateInterface().update();
    }

    public void updateSettingsLayout(){
        if(settingsFragment != null) settingsFragment.getUpdateInterface().update();
    }
//==================================================================================================
    //get interfaces

    public RouteListInterface getRouteListInterface(){
        return listFragment.getInterface();
    }
}
