package com.commeto.kuleuven.MP.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.commeto.kuleuven.MP.activities.RideDisplayActivity;
import com.commeto.kuleuven.MP.interfaces.RouteListInterface;
import com.commeto.kuleuven.MP.listeners.UnderlineButtonListener;
import com.commeto.kuleuven.MP.R;
import com.commeto.kuleuven.MP.sqlSupport.LocalDatabase;
import com.commeto.kuleuven.MP.sqlSupport.LocalRoute;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import kotlin.jvm.functions.Function2;

import static android.content.Context.MODE_PRIVATE;
import static com.commeto.kuleuven.MP.support.Static.timeFormat;

/**
 * Created by Jonas on 12/04/2018.
 *
 * Fragment to display a list of all the rides.
 */

public class RouteListFragment extends Fragment {
//==================================================================================================
    //constants

    private final String NAME = "Naam";
    private final String DOWN = "Aflopend";
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
    //interface

    private RouteListInterface routeListInterface = new RouteListInterface() {
        @Override
        public void resetList(Bundle options) {
            if (options != null) RouteListFragment.this.previous = options;
            localRouteList = getLocalRoutes();
            RouteListFragment.this.setView();
        }

        @Override
        public Bundle getPrevious(){
            return RouteListFragment.this.previous;
        }

        @Override
        public void setSearch(){
            RouteListFragment.this.search = null;
            setView();
        }
    };
//==================================================================================================
    //class specs

    private Bundle previous;
    private Context context;
    private LinearLayout list;

    private LocalDatabase localDatabase;
    private List<LocalRoute> localRouteList;
    private String search;

    private String user;
    private HashMap<String, Function2<String, Void, List<LocalRoute>>> sorting;
    private HashMap<String, Function2<View, LocalRoute, Void>> options;

    public RouteListInterface getInterface(){
        return routeListInterface;
    }

    public static RouteListFragment newInstance() {
        RouteListFragment fragment = new RouteListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
//==================================================================================================
    //lifecycle methods

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.options = new HashMap<>();
        this.sorting = new HashMap<>();
        context = getContext();
        user = context.getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE)
                .getString("username", "");
        localRouteList = LocalDatabase.getInstance(context).localRouteDAO().getAllByTimeDescending(user);

        //Default bundle for sorting.
        previous = new Bundle();
        previous.putString(SORT, DATE);
        previous.putString(BY, DOWN);

        localDatabase = LocalDatabase.getInstance(getContext());

        //Functions to call when ride has to be sorted. Contains queries for sorting.
        sorting.put(NAME, new Function2<String, Void, List<LocalRoute>>() {
            @Override
            public List<LocalRoute> invoke(String option, Void aVoid) {
                if (option.equals(DOWN)) {
                    return localDatabase.localRouteDAO().getAllByNameDescending(user);
                } else return localDatabase.localRouteDAO().getAllByNameAscending(user);
            }
        });
        sorting.put(DATE, new Function2<String, Void, List<LocalRoute>>() {
            @Override
            public List<LocalRoute> invoke(String option, Void aVoid) {
                if (option.equals(DOWN)) {
                    return localDatabase.localRouteDAO().getAllByTimeDescending(user);
                } else return localDatabase.localRouteDAO().getAllByTimeAscending(user);
            }
        });
        sorting.put(DURATION, new Function2<String, Void, List<LocalRoute>>() {
            @Override
            public List<LocalRoute> invoke(String option, Void aVoid) {
                if (option.equals(DOWN)) {
                    return localDatabase.localRouteDAO().getAllByDurationDescending(user);
                } else return localDatabase.localRouteDAO().getAllByDuratonAscending(user);
            }
        });
        sorting.put(SPEED, new Function2<String, Void, List<LocalRoute>>() {
            @Override
            public List<LocalRoute> invoke(String option, Void aVoid) {
                if (option.equals(DOWN)) {
                    return localDatabase.localRouteDAO().getAllBySpeedDescending(user);
                } else return localDatabase.localRouteDAO().getAllBySpeedAscending(user);
            }
        });
        sorting.put(DISTANCE, new Function2<String, Void, List<LocalRoute>>() {
            @Override
            public List<LocalRoute> invoke(String option, Void aVoid) {
                if (option.equals(DOWN)) {
                    return localDatabase.localRouteDAO().getAllByDistanceDescending(user);
                } else return localDatabase.localRouteDAO().getAllByDistanceAscending(user);
            }
        });

        //Default bundle to change layout depending on options chosen. contains functions to
        //change layout.
        options.put(DISTANCE, new Function2<View, LocalRoute, Void>() {
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
        options.put(SPEED, new Function2<View, LocalRoute, Void>() {
            @Override
            public Void invoke(View view, LocalRoute localRoute) {
                ((TextView) view.findViewById(R.id.speed)).setText(
                        String.format(Locale.getDefault(), "%.2f km/h", localRoute.getSpeed() * 3.6)
                );
                view.findViewById(R.id.speed_container).setVisibility(LinearLayout.VISIBLE);
                return null;
            }
        });
        options.put(DURATION, new Function2<View, LocalRoute, Void>() {
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

        search = null;
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragments_ride_list, container, false);

        ((SearchView) view.findViewById(R.id.list_search_bar)).setIconifiedByDefault(false);
        ((SearchView) view.findViewById(R.id.list_search_bar)).setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search = query.equals("") ? null : query;
                setView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.equals("")){
                    search = null;
                    setView();
                }
                return false;
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle bundle){
        super.onViewCreated(view, bundle);
        list = view.findViewById(R.id.route_list);
        view.findViewById(R.id.ride_list_search).setOnTouchListener(new UnderlineButtonListener(context));
        view.findViewById(R.id.sync_button).setOnTouchListener(new UnderlineButtonListener(context));
        view.findViewById(R.id.filter_sort).setOnTouchListener(new UnderlineButtonListener(context));
        setView();
    }

    @Override
    public void onStart(){
        super.onStart();
        setView();
    }

    /**
     * Method used to reset the list.
     */

    private void setView(){

        try {
            String option = previous.getString(SORT, DATE);
            if (list != null) {
                list.removeAllViews();
                LayoutInflater inflater = getLayoutInflater();

                DateFormat format = SimpleDateFormat.getDateTimeInstance();

                for (final LocalRoute localRoute : localRouteList) {

                    if (filterList(localRoute)) {
                        View view = inflater.inflate(R.layout.route_list_item, null);
                        ((TextView) view.findViewById(R.id.ride_name)).setText(
                                localRoute.getRidename()
                        );
                        ((TextView) view.findViewById(R.id.date)).setText(
                                format.format(new Date(localRoute.getTime()))
                        );

                        view.findViewById(R.id.outer).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(context, RideDisplayActivity.class);
                                intent.putExtra("id", localRoute.getLocalId());
                                startActivityForResult(intent, 0);
                            }
                        });

                        view.findViewById(R.id.cloud).setVisibility(localRoute.isSent() ?
                                View.VISIBLE : View.GONE
                        );

                        for (String key : previous.keySet()) {
                            if (previous.getBoolean(key, false)) {
                                try {
                                    options.get(key).invoke(view, localRoute);
                                } catch (Exception e) {
                                    //Empty, for safety.
                                }
                            }
                        }
                        if (options.containsKey(option))
                            options.get(option).invoke(view, localRoute);

                        list.addView(view);
                    }
                }
            }
        } catch (NullPointerException e){
            //Empty, just for safety in fragment.
        } catch (IllegalStateException e){
            //Empty, just for safety in fragment.
        }
    }

    /**
     * Method used to filter the list. Method checks if ride meets defined filter.
     *
     * @param route Route to be checked.
     * @return Boolean to represent if Route has to be displayed or not.
     */
    private boolean filterList(LocalRoute route){

        return
                (search == null || route.getRidename().contains(search)) &&
                (!previous.getBoolean(DISTANCE, false) || (route.getDistance() <= previous.getDouble(DISTANCE_UPPER, Double.MAX_VALUE) && route.getDistance() >= previous.getDouble(DISTANCE_LOWER, 0))) &&
                (!previous.getBoolean(DURATION, false) || (route.getDuration() <= previous.getLong(DURATION_UPPER, Long.MAX_VALUE) && route.getDuration() >= previous.getLong(DURATION_LOWER, 0))) &&
                (!previous.getBoolean(SPEED, false) || (route.getSpeed() <= previous.getDouble(SPEED_UPPER, Double.MAX_VALUE) && route.getSpeed() >= previous.getDouble(SPEED_LOWER, 0))) &&
                (!previous.getBoolean("Datum", false) || (route.getTime() <= previous.getLong(DATE_UPPER, Long.MAX_VALUE) && route.getTime() >= previous.getLong(DATE_LOWER, Long.MIN_VALUE)));
    }

    private List<LocalRoute> getLocalRoutes(){

        try {
            String sort;
            List<LocalRoute> localRoutes = localDatabase.localRouteDAO().getAll(user);
            if (sorting.containsKey((sort = previous.getString(SORT)))) {
                localRoutes = sorting.get(sort).invoke(previous.getString(BY), null);
            }
            return localRoutes;
        } catch (NullPointerException e){
            return null;
        }
    }
}
