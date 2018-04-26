package com.commeto.kuleuven.MP.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.commeto.kuleuven.MP.fragments.GlobalMapFragment;
import com.commeto.kuleuven.MP.fragments.HomeScreenFragment;
import com.commeto.kuleuven.MP.fragments.RouteListFragment;
import com.commeto.kuleuven.MP.fragments.SettingsFragment;
import com.commeto.kuleuven.MP.fragments.StatsFragment;
import com.commeto.kuleuven.MP.interfaces.RouteListInterface;

/**
 * <pre>
 * Created by Jonas on 13/04/2018.
 *
 * Adapter used for the fragments in the BaseActivity.
 *
 * Fragments:
 *  - HomeScreenFragment
 *  - StatsFragment
 *  - RouteListFragment
 *  - SettingsFragment
 *  - GlobalMapFragment
 * </pre>
 */

public class BasePagerAdapter extends FragmentPagerAdapter {

    private final int PAGE_COUNT = 5;

    private HomeScreenFragment homeScreenFragment;
    private StatsFragment statsFragment;
    private RouteListFragment listFragment;
    private SettingsFragment settingsFragment;
    private GlobalMapFragment globalMapFragment;

    /**
     * @param manager The fragment manager given by the calling activity.
     */
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

    /**
     * Method to update the content of the StatsFragment.
     */
    public void updateStatsLayout(){
        try {
            if (statsFragment != null) statsFragment.getUpdateInterface().update();
        } catch (IllegalStateException e){
            //Empty, used to make sure function doesn't get called when the fragment isn't attached
        }
    }

    /**
     * Method to update the SettingsFragment.
     */
    public void updateSettingsLayout(){
        try {
            if (settingsFragment != null && !settingsFragment.isDetached())
                settingsFragment.getUpdateInterface().update();
        } catch (IllegalStateException e){
            //Empty, used to make sure function doesn't get called when the fragment isn't attached
        }
    }
//==================================================================================================
    //get interfaces

    public RouteListInterface getRouteListInterface(){
        return listFragment.getInterface();
    }
}
