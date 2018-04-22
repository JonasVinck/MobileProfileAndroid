package com.commeto.kuleuven.MP.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;

/**
 * Created by Jonas on 6/04/2018.
 *
 * Dialog used to get a duration value.
 */

public class TimeDialog extends DialogFragment{

    private long time;
    private TimePickerDialog.OnTimeSetListener listener;

    /**
     * Method to set the default value to be displayed.
     *
     * @param time Default time.
     * @param listener OnTimeSetListener used.
     */

    public void set(long time, TimePickerDialog.OnTimeSetListener listener){
        this.time = time;
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle bundle){
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getActivity(),
                listener,
                getHours(),
                getMinutes(),
                true
        );
        timePickerDialog.setTitle("Selecteer een tijdsduur.");
        return timePickerDialog;
    }

    /**
     * @return The value of the currently selected hours.
     */
    private int getHours(){
        return (int) (time / 1000) / 3600;
    }

    /**
     * @return The value of the currently selected minutes.
     */
    private int getMinutes(){
        return (int) ((time / 1000) / 60) % 60;
    }
}
