package com.commeto.kuleuven.MP.fragments;


import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.commeto.kuleuven.MP.geoCoding.GeoCodeAdapter;
import com.commeto.kuleuven.MP.interfaces.MapStyleInterface;
import com.commeto.kuleuven.MP.listeners.UnderlineButtonListener;
import com.commeto.kuleuven.MP.R;
import com.commeto.kuleuven.MP.support.InternalIO;
import com.commeto.kuleuven.MP.views.MapOptionAdapter;
import com.mapbox.geocoder.service.models.GeocoderFeature;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

/**
 * Created by Jonas on 13/04/2018.
 */

public class GlobalMapFragment extends Fragment implements OnMapReadyCallback {

//==================================================================================================
    //class specs

    private MapStyleInterface mapStyleInterface = new MapStyleInterface() {
        @Override
        public void setStyle(String mapStyle) {
            if(mapView != null){
                setMap(mapStyle);
            }
            activity.findViewById(R.id.map_option_list).setVisibility(View.GONE);
        }
    };

    private Context context;
    private Activity activity;
    private MapView mapView;
    private MapboxMap map;
    private AutoCompleteTextView autocomplete;

//==================================================================================================
    //lifecycle methods

    public static GlobalMapFragment newInstance() {
        GlobalMapFragment fragment = new GlobalMapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        activity = getActivity();
        context = activity.getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_global_maps, container, false);

        view.findViewById(R.id.map_options).setOnTouchListener(new UnderlineButtonListener(context));
        view.findViewById(R.id.search).setOnTouchListener(new UnderlineButtonListener(context));
        ((TextView) view.findViewById(R.id.map_option)).setText(getResources().getStringArray(R.array.maps_options)[0]);

        Mapbox.getInstance(context, getResources().getString(R.string.jern_key));
        mapView = view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.setStyleUrl(getString(R.string.comfort_style));
        mapView.getMapAsync(this);

        final GeoCodeAdapter adapter = new GeoCodeAdapter(activity);
        autocomplete = view.findViewById(R.id.search_bar);
        autocomplete.setLines(1);
        autocomplete.setAdapter(adapter);
        autocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GeocoderFeature result = adapter.getItem(position);
                autocomplete.setText(result.getText());
                updateMap(result.getLatitude(), result.getLongitude());
                try {
                    ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
                } catch (NullPointerException e){
                    InternalIO.writeToLog(activity, e);
                }
            }
        });

        final Drawable imgClearButton = getResources().getDrawable(android.R.drawable.ic_menu_close_clear_cancel);
        autocomplete.setCompoundDrawablesWithIntrinsicBounds(null, null, imgClearButton, null);
        autocomplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AutoCompleteTextView et = (AutoCompleteTextView) v;
                autocomplete.setText("");
            }
        });

        try {
            ((ListView) view.findViewById(R.id.map_option_list)).setAdapter(new MapOptionAdapter(
                    getActivity(),
                    getActivity().getResources().getStringArray(R.array.maps_options),
                    getActivity().getResources().getStringArray(R.array.maps_options_descriptions),
                    mapStyleInterface
            ));
        } catch (NullPointerException e){
            InternalIO.writeToLog(getActivity(), e);
        }

        inflater.inflate(R.layout.view_comfort_legende, ((ViewGroup) view.findViewById(R.id.legend)));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop(){
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory(){
        super.onLowMemory();
        mapView.onLowMemory();
    }
//==================================================================================================
    //map functions

    public void updateMap(double lat, double lon){
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(lat, lon))
                .zoom(13)
                .build();
        map.setCameraPosition(cameraPosition);
    }
//==================================================================================================
    //onMapReadyCallBack

    @Override
    public void onMapReady(MapboxMap map){

        this.map = map;
        this.map.setCameraPosition(new CameraPosition.Builder().target(new LatLng(50.553, 4.484)).zoom(6).build());
    }
//==================================================================================================
    //private functions

    private void setMap(String option){
        ((LinearLayout) activity.findViewById(R.id.legend)).removeAllViews(); //todo nice fuckup
        activity.findViewById(R.id.search_bar_container).setBackgroundColor(getResources().getColor(R.color.white));
        ((AutoCompleteTextView) activity.findViewById(R.id.search_bar)).setTextColor(getResources().getColor(R.color.black));
        LayoutInflater inflater = getLayoutInflater();
        switch (option){
            case "Fietsverkeer":
                mapView.setStyleUrl(getString(R.string.fietsverkeer_style));
                inflater.inflate(R.layout.view_busy_legende, ((ViewGroup) activity.findViewById(R.id.legend)));
                break;
            case "Verlichting":
                activity.findViewById(R.id.search_bar_container).setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                ((AutoCompleteTextView) activity.findViewById(R.id.search_bar)).setTextColor(getResources().getColor(R.color.white));
                mapView.setStyleUrl(getString(R.string.lightlevel_style));
                inflater.inflate(R.layout.view_light_legende, ((ViewGroup) activity.findViewById(R.id.legend)));
                break;
            default:
                mapView.setStyleUrl(getString(R.string.comfort_style));
                inflater.inflate(R.layout.view_comfort_legende, ((ViewGroup) activity.findViewById(R.id.legend)));
                break;
        }
        activity.findViewById(R.id.map_option_list).setVisibility(View.GONE);
        ((TextView) activity.findViewById(R.id.map_option)).setText(option);
    }
}
