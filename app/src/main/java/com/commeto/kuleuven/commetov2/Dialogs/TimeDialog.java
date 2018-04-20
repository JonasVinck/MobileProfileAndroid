package com.commeto.kuleuven.commetov2.Dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;

import java.util.Date;

/**
 * Created by Jonas on 6/04/2018.
 */

public class TimeDialog extends DialogFragment{

    private long time;
    private TimePickerDialog.OnTimeSetListener listener;

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

    private int getHours(){
        return (int) (time / 1000) / 3600;
    }

    private int getMinutes(){
        return (int) ((time / 1000) / 60) % 60;
    }
}
