package com.commeto.kuleuven.MP.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.commeto.kuleuven.MP.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Jonas on 10/04/2018.
 *
 * Used to dispaly current locaiton while riding.
 */

public class MeasuringMapFragment extends Fragment{

    private Context context;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private Marker mapMarker;

    public static MeasuringMapFragment newInstance(Context context) {
        MeasuringMapFragment fragment = new MeasuringMapFragment();
        fragment.setContext(context);
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull  LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle bundle){
        super.onViewCreated(view, bundle);

        Mapbox.getInstance(context, getResources().getString(R.string.jern_key));

        mapView = view.findViewById(R.id.map);
        mapView.onCreate(bundle);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                mapboxMap.setZoom(16);
                mapboxMap.getUiSettings().setAllGesturesEnabled(false);
                mapMarker = mapboxMap.addMarker(new MarkerOptions().setPosition(
                        new LatLng(0, 0))
                        .setIcon(IconFactory.getInstance(context).fromResource(R.drawable.mapbox_mylocation_icon_default))
                );

                MeasuringMapFragment.this.mapboxMap = mapboxMap;
            }
        });
    }

    /**
     * Method used to add context to fragment. Context needed to initiate map.
     *
     * @param context Application context.
     */
    public void setContext(Context context){
        this.context = context;
    }

    /**
     * Method used to refresh current location.
     *
     * @param lat Current latitude.
     * @param lon Current Longitude.
     */
    public void append(double lat, double lon){

        mapMarker.setPosition(new LatLng(lat, lon));
        mapboxMap.animateCamera(new CameraUpdate() {
            @Nullable
            @Override
            public CameraPosition getCameraPosition(@NonNull MapboxMap mapboxMap) {
                return new CameraPosition.Builder()
                        .target(mapMarker.getPosition())
                        .build();
            }
        });
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
