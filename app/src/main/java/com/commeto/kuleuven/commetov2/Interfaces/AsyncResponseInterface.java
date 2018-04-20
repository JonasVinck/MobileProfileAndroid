package com.commeto.kuleuven.commetov2.Interfaces;

import com.commeto.kuleuven.commetov2.DataClasses.HTTPResponse;

/**
 * Created by Jonas on 4/03/2018.
 */

public interface AsyncResponseInterface {

    void processFinished(HTTPResponse response);
}
