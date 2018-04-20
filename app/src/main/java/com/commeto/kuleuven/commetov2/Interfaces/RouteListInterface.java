package com.commeto.kuleuven.commetov2.Interfaces;

import android.os.Bundle;

/**
 * Created by Jonas on 12/04/2018.
 */

public interface RouteListInterface {
    void resetList(Bundle options);
    Bundle getPrevious();
    void setSearch();
}
