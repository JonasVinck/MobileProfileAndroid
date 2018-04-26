package com.commeto.kuleuven.MP.exceptions;

/**
 * <pre>
 * Created by Jonas on 31/03/2018.
 *
 * Exception to be thrown when a ride has no distance.
 * </pre>
 */

public class NoDistanceException extends Exception{

    public NoDistanceException(String message){
        super(message);
    }
}
