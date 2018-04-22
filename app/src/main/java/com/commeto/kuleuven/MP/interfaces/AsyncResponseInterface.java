package com.commeto.kuleuven.MP.interfaces;

import com.commeto.kuleuven.MP.dataClasses.HTTPResponse;

/**
 * Created by Jonas on 4/03/2018.
 */

public interface AsyncResponseInterface {

    void processFinished(HTTPResponse response);
}
