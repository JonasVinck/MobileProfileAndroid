package com.commeto.kuleuven.MP.interfaces;

import android.os.Bundle;

/**
 * Created by Jonas on 12/04/2018.
 *
 * <p>
 * Interface used to communicate with the RouteListFragment.
 * </p>
 */

public interface RouteListInterface {
    void resetList(Bundle options);
    Bundle getPrevious();
    void resetList();
}
