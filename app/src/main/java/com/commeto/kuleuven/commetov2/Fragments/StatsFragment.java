package com.commeto.kuleuven.commetov2.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.commeto.kuleuven.commetov2.Activities.RideDisplayActivity;
import com.commeto.kuleuven.commetov2.Interfaces.LayoutUpdateInterface;
import com.commeto.kuleuven.commetov2.R;
import com.commeto.kuleuven.commetov2.SQLSupport.LocalDatabase;
import com.commeto.kuleuven.commetov2.SQLSupport.LocalRoute;
import com.commeto.kuleuven.commetov2.SQLSupport.LocalRouteDAO;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import kotlin.jvm.functions.Function2;

import static com.commeto.kuleuven.commetov2.Support.Static.timeFormat;

/**
 * Created by Jonas on 12/04/2018.
 */

public class StatsFragment extends Fragment{
//==================================================================================================
    private LayoutUpdateInterface updateInterface = new LayoutUpdateInterface() {
        @Override
        public void update() {
            updateLayout();
        }
    };

//==================================================================================================
    //class specs
    private HashMap<String, Function2<View, LocalRoute, Void>> options;
    private Activity activity;

//==================================================================================================
    //lifecycle methods

    public static StatsFragment newInstance() {
        StatsFragment fragment = new StatsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.activity = getActivity();
        options = new HashMap<>();

        options.put("afstand", new Function2<View, LocalRoute, Void>() {
            @Override
            public Void invoke(View view, LocalRoute localRoute) {
                double distance;
                ((TextView) view.findViewById(R.id.distance)).setText(
                        (distance = localRoute.getDistance()) > 1000 ?
                                String.format(Locale.getDefault(), "%f km", distance / 1000) :
                                String.format(Locale.getDefault(), "%f m", distance)
                );
                view.findViewById(R.id.distance_container).setVisibility(LinearLayout.VISIBLE);
                return null;
            }
        });
        options.put("snelheid", new Function2<View, LocalRoute, Void>() {
            @Override
            public Void invoke(View view, LocalRoute localRoute) {
                ((TextView) view.findViewById(R.id.speed)).setText(
                        String.format(Locale.getDefault(), "%.2f km/h", localRoute.getSpeed() * 3.6)
                );
                view.findViewById(R.id.speed_container).setVisibility(LinearLayout.VISIBLE);
                return null;
            }
        });
        options.put("duur", new Function2<View, LocalRoute, Void>() {
            @Override
            public Void invoke(View view, LocalRoute localRoute) {
                int[] time = timeFormat(localRoute.getDuration());
                ((TextView) view.findViewById(R.id.duration)).setText(time.length == 3 ?
                        String.format(Locale.getDefault(), "%d uur, %d min, %d sec", time[0], time[1], time[2]) :
                        String.format(Locale.getDefault(),"%d min, %d sec", time[0], time[1])
                );
                view.findViewById(R.id.duration_container).setVisibility(LinearLayout.VISIBLE);
                return null;
            }
        });
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle bundle){
        super.onViewCreated(view, bundle);

        updateLayout();
    }
//==================================================================================================
    //private functions

    private View generateView(final LocalRoute localRoute, String option){

        LayoutInflater inflater = getLayoutInflater();
        DateFormat format = SimpleDateFormat.getDateTimeInstance();
        View view = inflater.inflate(R.layout.route_list_item, null);
        ((TextView) view.findViewById(R.id.route_name)).setText(
                localRoute.getRidename()
        );
        ((TextView) view.findViewById(R.id.date)).setText(
                format.format(new Date(localRoute.getTime()))
        );

        view.findViewById(R.id.outer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), RideDisplayActivity.class);
                intent.putExtra("id", localRoute.getLocalId());
                startActivityForResult(intent, 0);
            }
        });
        view.findViewById(R.id.outer).setBackgroundColor(getResources().getColor(R.color.white));

        options.get(option).invoke(view, localRoute);

        return view;
    }

    private void updateLayout(){

        SharedPreferences preferences = activity.getSharedPreferences(
                "commeto", Context.MODE_PRIVATE
        );
        ((TextView) activity.findViewById(R.id.username)).setText(preferences.getString("username", "dit zou ge nooit mogen zien"));

        LocalRouteDAO dao = LocalDatabase.getInstance(getContext()).localRouteDAO();
        List<LocalRoute> routes = dao.getAll(preferences.getString("username", ""));
        double totalAverageKm = 0, averageSpeed = 0, topSpeed = 0, topDistance = 0;
        LocalRoute fastest = new LocalRoute(), longest = new LocalRoute(), furthest = new LocalRoute();
        long totaltAverageDuration = 0, topduration = 0;
        for(LocalRoute localRoute: routes){
            if(localRoute.getSpeed() > topSpeed){
                topSpeed = localRoute.getSpeed();
                fastest = localRoute;
            }
            if(localRoute.getDistance() > topDistance){
                topDistance = localRoute.getDistance();
                furthest = localRoute;
            }
            if(localRoute.getDuration() > topduration){
                topduration = localRoute.getDuration();
                longest = localRoute;
            }

            totalAverageKm+=localRoute.getDistance();
            averageSpeed+=localRoute.getSpeed();
            totaltAverageDuration+=localRoute.getDuration();
        }

        ((TextView) activity.findViewById(R.id.total_ride)).setText(String.format(
                Locale.getDefault(),
                "%d",
                routes.size()
        ));
        ((TextView) activity.findViewById(R.id.total_ride_km)).setText(String.format(
                Locale.getDefault(),
                "%.2f km",
                totalAverageKm / 1000
        ));
        int forAverage = routes.size();
        if(forAverage <= 0) forAverage = 1;
        int[] time = timeFormat( totaltAverageDuration / forAverage);
        ((TextView) activity.findViewById(R.id.total_average_duration)).setText(
                time.length == 3 ?
                        String.format(
                                Locale.getDefault(),
                                "%d:%d:%d",
                                time[0], time[1], time[2]
                        ) :
                        String.format(
                                Locale.getDefault(),
                                "%d:%d",
                                time[0], time[1]
                        )
        );
        ((TextView) activity.findViewById(R.id.average_speed)).setText(String.format(
                Locale.getDefault(),
                "%.2f km/h",
                (float) (averageSpeed / forAverage) * 3.6f
        ));
        ((TextView) activity.findViewById(R.id.top_speed)).setText(String.format(
                Locale.getDefault(),
                "%.2f km/h",
                (float) (topSpeed * 3.6f)
        ));
        ((TextView) activity.findViewById(R.id.total_average_km)).setText(String.format(
                Locale.getDefault(),
                "%.2f km",
                (float) totalAverageKm / forAverage / 1000
        ));

        ((LinearLayout) activity.findViewById(R.id.fastest_ride)).addView(
                generateView(fastest, "snelheid")
        );
        ((LinearLayout) activity.findViewById(R.id.furthest_ride)).addView(
                generateView(furthest, "afstand")
        );
        ((LinearLayout) activity.findViewById(R.id.longest_ride)).addView(
                generateView(longest, "duur")
        );
    }
//==================================================================================================
    //public functions

    public LayoutUpdateInterface getUpdateInterface(){
        return this.updateInterface;
    }
}
