package com.commeto.kuleuven.MP.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import java.util.Calendar;

/**
 * <pre>
 * Created by Jonas on 6/04/2018.
 *
 * Dialog used to get a date.
 * </pre>
 */

public class DateDialog extends DialogFragment{

    private Calendar date;
    private DatePickerDialog.OnDateSetListener listener;

    /**
     * Method to set the default date displayed.
     *
     * @param date      Date to be set as standard.
     * @param listener  Used OnDateChangedListener to return the information.
     */
    public void set(Calendar date, DatePickerDialog.OnDateSetListener listener){
        this.date = date;
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle bundle){
        return new DatePickerDialog(
                getActivity(),
                listener,
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH)
        );
    }
}
