package com.commeto.kuleuven.commetov2.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.commeto.kuleuven.commetov2.Fragments.RouteListFragment;
import com.commeto.kuleuven.commetov2.Fragments.StatsFragment;
import com.commeto.kuleuven.commetov2.Interfaces.RouteListInterface;

/**
 * Created by Jonas on 12/04/2018.
 */

public class ProfilePagerAdapter extends FragmentPagerAdapter {

    private int PAGE_COUNT;
    private StatsFragment statsFragment;
    private RouteListFragment routeListFragment;

    public ProfilePagerAdapter(FragmentManager fragmentManager, int page_count){
        super(fragmentManager);
        this.PAGE_COUNT = page_count;
        statsFragment = StatsFragment.newInstance();
        routeListFragment = RouteListFragment.newInstance();
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 1:
                return routeListFragment;
            default:
                return statsFragment;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 1:
                return "Ritten";
            default:
                return "Statistieken";
        }
    }

    public RouteListInterface getInterface(){
        return routeListFragment.getInterface();
    }
}
