package com.commeto.kuleuven.commetov2.Activities;

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

import com.commeto.kuleuven.commetov2.Dialogs.DateDialog;
import com.commeto.kuleuven.commetov2.Dialogs.TimeDialog;
import com.commeto.kuleuven.commetov2.Listeners.UnderlineButtonListener;
import com.commeto.kuleuven.commetov2.R;

import java.text.DateFormat;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.commeto.kuleuven.commetov2.Support.Static.makeToastLong;
import static com.commeto.kuleuven.commetov2.Support.Static.timeFormat;
import static com.commeto.kuleuven.commetov2.Support.Static.timeToString;
import static java.lang.Math.min;
import static java.lang.Math.multiplyExact;
import static java.lang.Math.round;

/**
 * Created by Jonas on 1/03/2018.
 */

public class FilterSortActivity extends AppCompatActivity{
//==================================================================================================
    //class specs

    private Context context;
    private Bundle options;

    private Calendar start_date, end_date;
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
            } else makeToastLong(context, "Begindatum kan niet na einddatum liggen.");
        }
    };
    private DatePickerDialog.OnDateSetListener endDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            Calendar temp;
            (temp = Calendar.getInstance()).set(year, month, day, 0, 0, 0);
            if(start_date.getTime().before(temp.getTime())) {
                end_date = temp;

                ((TextView) findViewById(R.id.end_date)).setText(
                        DateFormat.getDateInstance().format(end_date.getTime())
                );
            } else makeToastLong(context, "Einddatum kan niet voor begindatum liggen.");
        }
    };

    private Long duration_lower, duration_upper;
    private TimePickerDialog.OnTimeSetListener durationLowerListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
            long temp = hours * 3600 * 1000 + minutes * 60 * 1000;

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
    private TimePickerDialog.OnTimeSetListener getDurationUpperListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
            long temp = hours * 3600 * 1000 + minutes * 60 * 1000;

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
        super.onCreate(bundle);
        setContentView(R.layout.activity_filter_sort);
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

    public void setStartDate(View view){
        DateDialog dateDialog = new DateDialog();
        dateDialog.set(start_date, startDateListener);
        dateDialog.show(getFragmentManager(), "datepicker");
    }

    public void setEndDate(View view){
        DateDialog dateDialog = new DateDialog();
        dateDialog.set(end_date, endDateListener);
        dateDialog.show(getFragmentManager(), "datepicker");
    }

    public void setDurationLower(View view){
        TimeDialog timeDialog = new TimeDialog();
        timeDialog.set(duration_lower, durationLowerListener);
        timeDialog.show(getFragmentManager(), "timepicker");
    }

    public void setDurationUpper(View view){
        TimeDialog timeDialog = new TimeDialog();
        timeDialog.set(duration_upper, getDurationUpperListener);
        timeDialog.show(getFragmentManager(), "timepicker");
    }

    public void clear(View view){
        this.options = new Bundle();
        options.putString("sort", "Datum");
        options.putString("by", "Aflopend");

    }

    public void confirm(View view){

        boolean temp;
        String tempString;

        options.putString(
                "sort",
                ((Spinner) findViewById(R.id.attribute)).getSelectedItem().toString()
        );
        options.putString(
                "by",
                ((Spinner) findViewById(R.id.by)).getSelectedItem().toString()
        );

        options.putBoolean(
                "Datum",
                (temp = (((Switch) findViewById(R.id.date_switch)).isChecked()))
        );
        options.putLong(
                "start_date",
                temp ? start_date.getTimeInMillis() : 0
        );
        options.putLong(
                "end_date",
                temp ? end_date.getTimeInMillis() : System.currentTimeMillis()
        );
        long test = start_date.getTimeInMillis();
        options.putBoolean(
                "Duur",
                (temp = (((Switch) findViewById(R.id.duration_switch)).isChecked()))
        );
        options.putLong(
                "duration_lower",
                duration_lower
        );
        options.putLong(
                "duration_upper",
                duration_upper
        );

        options.putBoolean(
                "Snelheid",
                temp = (((Switch) findViewById(R.id.speed_switch)).isChecked())
        );
        options.putDouble(
                "speed_lower",
                temp && !(tempString = ((EditText) findViewById(R.id.speed_lower)).getText().toString()).equals("")
                        ? Double.parseDouble(tempString) : Double.MIN_VALUE
        );
        options.putDouble(
                "speed_upper",
                temp && !(tempString = ((EditText) findViewById(R.id.speed_upper)).getText().toString()).equals("")
                        ? Double.parseDouble(tempString) : Double.MAX_VALUE
        );

        options.putBoolean(
                "Afstand",
                temp = (((Switch) findViewById(R.id.distance_switch)).isChecked())
        );
        options.putDouble(
                "distance_lower",
                temp && !(tempString = ((EditText) findViewById(R.id.distance_lower)).getText().toString()).equals("")
                        ? Double.parseDouble(tempString) : Double.MIN_VALUE
        );
        options.putDouble(
                "distance_upper",
                temp && !(tempString = ((EditText) findViewById(R.id.distance_upper)).getText().toString()).equals("")
                        ? Double.parseDouble(tempString) : Double.MAX_VALUE
        );

        setResult(RESULT_OK, new Intent().putExtra("options", options));
        finish();
    }
//==================================================================================================
    //private functions

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
            if(array[i].equals(options.getString("sort", ""))) position = i;
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
            if(array[i].equals(options.getString("by", ""))) position = i;
        }
        spinner.setSelection(position);

        double temp;
        Switch tempSwitch;
        long tempLong;

        if((tempLong = options.getLong("start_date", 0)) != 0) {
            (start_date = Calendar.getInstance()).setTimeInMillis(tempLong);
        } else {
            (start_date = Calendar.getInstance()).setTimeInMillis(System.currentTimeMillis());
        }

        if((tempLong = options.getLong("end_date", 0)) != 0) {
            (end_date = Calendar.getInstance()).setTimeInMillis(tempLong);
        } else {
            (end_date = Calendar.getInstance()).setTimeInMillis(System.currentTimeMillis());
        }

        duration_lower = options.getLong("duration_lower", 0);
        duration_upper = options.getLong("duration_upper", 0);

        if((tempLong = options.getLong("end_date", 0)) != 0) {
            (end_date = Calendar.getInstance()).setTimeInMillis(tempLong);
        } else {
            (end_date = Calendar.getInstance()).setTimeInMillis(System.currentTimeMillis());
        }

        (tempSwitch = findViewById(R.id.date_switch)).setChecked(options.getBoolean("Datum", false));
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
                "Duur",
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
                "Afstand",
                false)
        );
        findViewById(R.id.distance_container).setVisibility(
                tempSwitch.isChecked() ? View.VISIBLE : View.GONE
        );
        ((EditText) findViewById(R.id.distance_lower)).setText(
                (temp = options.getDouble("distance_lower", Double.MIN_VALUE)) != Double.MIN_VALUE ?
                        Double.toString(temp) : ""
        );
        ((EditText) findViewById(R.id.distance_upper)).setText(
                (temp = options.getDouble("distance_upper", Double.MAX_VALUE)) != Double.MAX_VALUE ?
                        Double.toString(temp) : ""
        );

        (tempSwitch = findViewById(R.id.speed_switch)).setChecked(options.getBoolean(
                "Snelheid",
                false)
        );
        findViewById(R.id.speed_container).setVisibility(
                tempSwitch.isChecked() ? View.VISIBLE : View.GONE
        );
        ((EditText) findViewById(R.id.speed_lower)).setText(
                (temp = options.getDouble("speed_lower", Double.MIN_VALUE)) != Double.MIN_VALUE ?
                        Double.toString(temp) : ""
        );
        ((EditText) findViewById(R.id.speed_upper)).setText(
                (temp = options.getDouble("speed_upper", Double.MAX_VALUE)) != Double.MAX_VALUE ?
                        Double.toString(temp) : ""
        );
    }
}