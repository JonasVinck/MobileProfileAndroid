package com.commeto.kuleuven.MP.interfaces;

import com.commeto.kuleuven.MP.dataClasses.HTTPResponse;

/**
 * Created by Jonas on 4/03/2018.
 *
 * <p>
 * Interface used to communicate HTTPResponse objects between AsyncTasks and calling Activities.
 * </p>
 */

public interface AsyncResponseInterface {

    void processFinished(HTTPResponse response);
}
