package com.commeto.kuleuven.commetov2.Dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Jonas on 6/04/2018.
 */

public class DateDialog extends DialogFragment{

    private Calendar date;
    private DatePickerDialog.OnDateSetListener listener;

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
