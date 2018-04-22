package com.commeto.kuleuven.MP.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.commeto.kuleuven.MP.dialogs.DateDialog;
import com.commeto.kuleuven.MP.dialogs.TimeDialog;
import com.commeto.kuleuven.MP.listeners.UnderlineButtonListener;
import com.commeto.kuleuven.MP.R;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

import static com.commeto.kuleuven.MP.support.Static.makeToastLong;
import static com.commeto.kuleuven.MP.support.Static.timeToString;
import static java.lang.Math.min;
import static java.lang.Math.multiplyExact;
import static java.lang.Math.round;

/**
 * Created by Jonas on 1/03/2018.
 */

public class FilterSortActivity extends AppCompatActivity{
//==================================================================================================
    //constants

    private final String SORT = "sort";
    private final String BY = "by";
    private final String DATE = "Datum";
    private final String DATE_LOWER = "start_date";
    private final String DATE_UPPER = "end_date";
    private final String DURATION = "Duur";
    private final String DURATION_LOWER = "duration_lower";
    private final String DURATION_UPPER = "duration_upper";
    private final String SPEED = "Snelheid";
    private final String SPEED_LOWER = "speed_lower";
    private final String SPEED_UPPER = "speed_upper";
    private final String DISTANCE = "Afstand";
    private final String DISTANCE_LOWER = "distance_lower";
    private final String DISTANCE_UPPER = "distance_upper";
//==================================================================================================
    //class specs

    private Context context;
    private Bundle options;

    private Calendar start_date, end_date;

    /**
     * Gets date from dialog and sets beginning date to filter.
     */
    private DatePickerDialog.OnDateSetListener startDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            Calendar temp;
            (temp = Calendar.getInstance()).set(year, month, day, 0, 0, 0);
            if(end_date.getTime().after(temp.getTime())) {
                start_date = temp;

                ((TextView) findViewById(R.id.start_date)).setText(
                        DateFormat.getDateInstance().format(start_date.getTime())
                );
            } else makeToastLong(context, getString(R.string.beginning_date_before_end));
        }
    };

    /**
     * Gets date from dialog and sets end date to filter.
     */
    private DatePickerDialog.OnDateSetListener endDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            Calendar temp = Calendar.getInstance();
            temp.set(year, month, day, 0, 0, 0);
            if(start_date.getTime().before(temp.getTime())) {
                end_date = temp;

                ((TextView) findViewById(R.id.end_date)).setText(
                        DateFormat.getDateInstance().format(end_date.getTime())
                );
            } else makeToastLong(context, getString(R.string.end_date_before_beginning));
        }
    };

    private Long duration_lower, duration_upper;

    /**
     * Gets duration from dialog and sets lower bound for duration.
     */

    private TimePickerDialog.OnTimeSetListener durationLowerListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
            long temp = (long) hours * 3600 * 1000 + (long) minutes * 60 * 1000;

            if(temp <= duration_upper) {
                duration_lower = temp;

                ((TextView) findViewById(R.id.duration_lower)).setText(
                        String.format(Locale.getDefault(), "%du%d",
                                hours,
                                minutes
                        )
                );
            }
        }
    };

    /**
     * Gets duration from dialog and sets upper bound for duration.
     */
    private TimePickerDialog.OnTimeSetListener durationUpperListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
            long temp = (long) hours * 3600 * 1000 + (long) minutes * 60 * 1000;

            if(temp >= duration_lower) {
                duration_upper = temp;

                ((TextView) findViewById(R.id.duration_upper)).setText(
                        String.format(Locale.getDefault(), "%du%d",
                                hours,
                                minutes
                        )
                );
            }
        }
    };
//==================================================================================================
    //lifecycle methods

    @Override
    public void onCreate(Bundle bundle) {

        //Necessary to initiate activity.
        super.onCreate(bundle);
        setContentView(R.layout.activity_filter_sort);

        //Setting Attributes.
        context = getApplicationContext();
        options = getIntent().getBundleExtra("options");

        setLayout();
    }

    @Override
    public void onStart() {
        super.onStart();

        findViewById(R.id.confirm).setOnTouchListener(new UnderlineButtonListener(context));
        findViewById(R.id.clear).setOnTouchListener(new UnderlineButtonListener(context));

        ((Switch) findViewById(R.id.date_switch)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                findViewById(R.id.date_container).setVisibility(
                        b ? View.VISIBLE : View.GONE
                );
            }
        });
        ((Switch) findViewById(R.id.duration_switch)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                findViewById(R.id.duration_container).setVisibility(
                        b ? View.VISIBLE : View.GONE
                );
            }
        });
        ((Switch) findViewById(R.id.speed_switch)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                findViewById(R.id.speed_container).setVisibility(
                        b ? View.VISIBLE : View.GONE
                );
            }
        });
        ((Switch) findViewById(R.id.distance_switch)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                findViewById(R.id.distance_container).setVisibility(
                        b ? View.VISIBLE : View.GONE
                );
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
//==================================================================================================
    //button actions

    /**
     * Open dialog to set the lower bound for the date.
     *
     * @param view Unused but necessary for using as onClick in XML
     */

    public void setStartDate(View view){
        DateDialog dateDialog = new DateDialog();
        dateDialog.set(start_date, startDateListener);
        dateDialog.show(getFragmentManager(), "datepicker");
    }

    /**
     * Open dialog to set the upper bound for the date.
     *
     * @param view Unused but necessary for using as onClick in XML.
     */

    public void setEndDate(View view){
        DateDialog dateDialog = new DateDialog();
        dateDialog.set(end_date, endDateListener);
        dateDialog.show(getFragmentManager(), "datepicker");
    }

    /**
     * Open dialog to set the lower bound for the duration.
     *
     * @param view Unused but necessary for using as onClick in XML.
     */

    public void setDurationLower(View view){
        TimeDialog timeDialog = new TimeDialog();
        timeDialog.set(duration_lower, durationLowerListener);
        timeDialog.show(getFragmentManager(), "timepicker");
    }

    /**
     * Open dialog to set the upper bound for the duration.
     *
     * @param view Unused but necessary for using as onClick in XML.
     */

    public void setDurationUpper(View view){
        TimeDialog timeDialog = new TimeDialog();
        timeDialog.set(duration_upper, durationUpperListener);
        timeDialog.show(getFragmentManager(), "timepicker");
    }

    /**
     * Clear the current options Bundle.
     *
     * @param view Unused but necessary for using as onClick in XML
     */

    public void clear(View view){
        this.options = new Bundle();
        options.putString(SORT, DATE);
        options.putString(BY, "Aflopend");

    }

    /**
     * Bundle all the set parameters and finishes activity.
     *
     * @param view Unused but necessary for using as onClick in XML
     */

    public void confirm(View view){

        boolean temp;
        String tempString;

        options.putString(
                SORT,
                ((Spinner) findViewById(R.id.attribute)).getSelectedItem().toString()
        );
        options.putString(
                BY,
                ((Spinner) findViewById(R.id.by)).getSelectedItem().toString()
        );

        options.putBoolean(
                DATE,
                (temp = (((Switch) findViewById(R.id.date_switch)).isChecked()))
        );
        options.putLong(
                DATE_LOWER,
                temp ? start_date.getTimeInMillis() : 0
        );
        options.putLong(
                DATE_UPPER,
                temp ? end_date.getTimeInMillis() : System.currentTimeMillis()
        );
        long test = start_date.getTimeInMillis();
        options.putBoolean(
                DURATION,
                (temp = (((Switch) findViewById(R.id.duration_switch)).isChecked()))
        );
        options.putLong(
                DURATION_LOWER,
                duration_lower
        );
        options.putLong(
                DURATION_UPPER,
                duration_upper
        );

        options.putBoolean(
                SPEED,
                temp = (((Switch) findViewById(R.id.speed_switch)).isChecked())
        );
        options.putDouble(
                SPEED_LOWER,
                temp && !(tempString = ((EditText) findViewById(R.id.speed_lower)).getText().toString()).equals("")
                        ? Double.parseDouble(tempString) : Double.MIN_VALUE
        );
        options.putDouble(
                SPEED_UPPER,
                temp && !(tempString = ((EditText) findViewById(R.id.speed_upper)).getText().toString()).equals("")
                        ? Double.parseDouble(tempString) : Double.MAX_VALUE
        );

        options.putBoolean(
                DISTANCE,
                temp = (((Switch) findViewById(R.id.distance_switch)).isChecked())
        );
        options.putDouble(
                DISTANCE_LOWER,
                temp && !(tempString = ((EditText) findViewById(R.id.distance_lower)).getText().toString()).equals("")
                        ? Double.parseDouble(tempString) : Double.MIN_VALUE
        );
        options.putDouble(
                DISTANCE_UPPER,
                temp && !(tempString = ((EditText) findViewById(R.id.distance_upper)).getText().toString()).equals("")
                        ? Double.parseDouble(tempString) : Double.MAX_VALUE
        );

        setResult(RESULT_OK, new Intent().putExtra("options", options));
        finish();
    }
//==================================================================================================
    //private functions

    /**
     * Sets the layout to match the given options.
     */

    private void setLayout(){

        String[] array;
        int position = 0;
        Spinner spinner = findViewById(R.id.attribute);
        spinner.setAdapter(
                ArrayAdapter.createFromResource(
                        this,
                        R.array.sorter_options,
                        R.layout.layout_spinner_text
                )
        );
        array = getResources().getStringArray(R.array.sorter_options);
        for (int i = 0; i < array.length; i++){
            if(array[i].equals(options.getString(SORT, ""))) position = i;
        }
        spinner.setSelection(position);

        spinner = findViewById(R.id.by);
        spinner.setAdapter(
                ArrayAdapter.createFromResource(
                        this,
                        R.array.order_options,
                        R.layout.layout_spinner_text_small
                )
        );
        array = getResources().getStringArray(R.array.order_options);
        position = 0;
        for (int i = 0; i < array.length; i++){
            if(array[i].equals(options.getString(BY, ""))) position = i;
        }
        spinner.setSelection(position);

        double temp;
        Switch tempSwitch;
        long tempLong;

        tempLong = options.getLong(DATE_LOWER, 0);
        if(tempLong != 0) (start_date = Calendar.getInstance()).setTimeInMillis(tempLong);
        else (start_date = Calendar.getInstance()).setTimeInMillis(System.currentTimeMillis());

        tempLong = options.getLong(DATE_UPPER, 0);
        if(tempLong != 0) (end_date = Calendar.getInstance()).setTimeInMillis(tempLong);
        else (end_date = Calendar.getInstance()).setTimeInMillis(System.currentTimeMillis());

        duration_lower = options.getLong(DURATION, 0);
        duration_upper = options.getLong(DURATION_UPPER, 0);

        (tempSwitch = findViewById(R.id.date_switch)).setChecked(options.getBoolean(DATE, false));
        findViewById(R.id.date_container).setVisibility(
                tempSwitch.isChecked() ? View.VISIBLE : View.GONE
        );
        ((TextView) findViewById(R.id.start_date)).setText(
                DateFormat.getDateInstance().format(start_date.getTime())
        );
        ((TextView) findViewById(R.id.end_date)).setText(
                DateFormat.getDateInstance().format(end_date.getTime())
        );

        (tempSwitch = findViewById(R.id.duration_switch)).setChecked(options.getBoolean(
                DURATION,
                false
        ));
        findViewById(R.id.duration_container).setVisibility(
                tempSwitch.isChecked() ? View.VISIBLE : View.GONE
        );

        ((TextView) findViewById(R.id.duration_lower)).setText(
                timeToString(duration_lower)
        );
        ((TextView) findViewById(R.id.duration_upper)).setText(
                timeToString(duration_upper)
        );

        (tempSwitch = findViewById(R.id.distance_switch)).setChecked(options.getBoolean(
                DISTANCE,
                false)
        );
        findViewById(R.id.distance_container).setVisibility(
                tempSwitch.isChecked() ? View.VISIBLE : View.GONE
        );
        ((EditText) findViewById(R.id.distance_lower)).setText(
                (temp = options.getDouble(DISTANCE_LOWER, Double.MIN_VALUE)) != Double.MIN_VALUE ?
                        Double.toString(temp) : ""
        );
        ((EditText) findViewById(R.id.distance_upper)).setText(
                (temp = options.getDouble(DISTANCE_UPPER, Double.MAX_VALUE)) != Double.MAX_VALUE ?
                        Double.toString(temp) : ""
        );

        (tempSwitch = findViewById(R.id.speed_switch)).setChecked(options.getBoolean(
                SPEED,
                false)
        );
        findViewById(R.id.speed_container).setVisibility(
                tempSwitch.isChecked() ? View.VISIBLE : View.GONE
        );
        ((EditText) findViewById(R.id.speed_lower)).setText(
                (temp = options.getDouble(SPEED_LOWER, Double.MIN_VALUE)) != Double.MIN_VALUE ?
                        Double.toString(temp) : ""
        );
        ((EditText) findViewById(R.id.speed_upper)).setText(
                (temp = options.getDouble(SPEED_UPPER, Double.MAX_VALUE)) != Double.MAX_VALUE ?
                        Double.toString(temp) : ""
        );
    }
}