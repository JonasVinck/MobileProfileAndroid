package com.commeto.kuleuven.commetov2.Fragments;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.commeto.kuleuven.commetov2.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Jonas on 10/04/2018.
 */

public class MeasuringMapFragment extends Fragment{

    private String title;
    private int page;
    private Context context;
    private MapView mapView;
    private MapboxMap mapboxMap;

    private LinkedList<LatLng> coords;

    public static MeasuringMapFragment newInstance(int page, String title, Context context) {
        MeasuringMapFragment fragment = new MeasuringMapFragment();
        fragment.setContext(context);
        Bundle args = new Bundle();
        args.putInt("page", page);
        args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("page", 0);
        title = getArguments().getString("title");
        coords = new LinkedList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle bundle){
        super.onViewCreated(view, bundle);

        Mapbox.getInstance(context, getResources().getString(R.string.mapbox_key));

        mapView = view.findViewById(R.id.map);
        mapView.onCreate(bundle);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                mapboxMap.setTilt(45);
                mapboxMap.setZoom(16);
                MeasuringMapFragment.this.mapboxMap = mapboxMap;
            }
        });
    }

    public void setContext(Context context){
        this.context = context;
    }

    public String getTitle() {
        return title;
    }

    public void append(double lat, double lon, double bearing){

        this.coords.add(new LatLng(lat, lon));
        if(coords.size() > 60) coords.removeFirst();

        mapboxMap.clear();
        mapboxMap.addMarker(new MarkerOptions().setPosition(coords.getLast()));
        mapboxMap.moveCamera(CameraUpdateFactory.newLatLng(coords.getLast()));
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
