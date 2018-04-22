package com.commeto.kuleuven.MP.interfaces;

import android.os.Bundle;

/**
 * Created by Jonas on 12/04/2018.
 *
 * Interface used to communicate with the RouteListFragment.
 */

public interface RouteListInterface {
    void resetList(Bundle options);
    Bundle getPrevious();
    void setSearch();
}
