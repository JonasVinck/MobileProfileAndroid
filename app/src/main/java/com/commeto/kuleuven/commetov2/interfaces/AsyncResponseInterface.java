package com.commeto.kuleuven.commetov2.interfaces;

import com.commeto.kuleuven.commetov2.dataClasses.HTTPResponse;

/**
 * Created by Jonas on 4/03/2018.
 */

public interface AsyncResponseInterface {

    void processFinished(HTTPResponse response);
}
