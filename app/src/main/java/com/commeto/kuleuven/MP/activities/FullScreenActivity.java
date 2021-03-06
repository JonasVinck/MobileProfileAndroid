package com.commeto.kuleuven.MP.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.commeto.kuleuven.MP.listeners.UnderlineButtonListener;
import com.commeto.kuleuven.MP.R;
import com.commeto.kuleuven.MP.sqlSupport.LocalDatabase;
import com.commeto.kuleuven.MP.sqlSupport.LocalRoute;
import com.commeto.kuleuven.MP.support.MapSupport;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.List;

/**
 * Created by Jonas on 1/03/2018.
 *
 * Activity used to display a given route fullscreen.
 *
 * Uses:
 *  - MapSupport
 */

public class FullScreenActivity extends AppCompatActivity implements OnMapReadyCallback {
//==================================================================================================
    //class specs

    private Context context;
    private LocalRoute localRoute;
    private MapView mapView;
    private MapboxMap map;
    private LatLngBounds bounds;

//==================================================================================================
    //lifecycle methods

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_fullscreen_map);
        context = getApplicationContext();

        int id = getIntent().getIntExtra("id", 0);
        List<LocalRoute> localRoutes = LocalDatabase.getInstance(context).localRouteDAO().exists(
                id,
                getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE).getString("username", "")
        );
        if(!localRoutes.isEmpty()) localRoute = localRoutes.get(0);
        else finish();
        Mapbox.getInstance(context, getResources().getString(R.string.jern_key));
        mapView = findViewById(R.id.map);
        mapView.onCreate(bundle);
        mapView.getMapAsync(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();

        findViewById(R.id.center).setOnTouchListener(new UnderlineButtonListener(context));
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
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory(){
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
//==================================================================================================
    //onMapReadyCallBack

    /**
     * Override for the onMapReadyCallback. When the map is ready, the ride can be added to the map.
     *
     * @param map The initiated map.
     */
    @Override
    public void onMapReady(MapboxMap map){

        this.map = map;

        MapSupport mapSupport = new MapSupport(context, localRoute);

        mapSupport.displayRide(map);
        bounds = mapSupport.getBounds();
        center(null);
    }
//==================================================================================================
    //button functions

    /**
     * Method used to center the ride on the map.
     * The view parameter is never used, but is necessary in order to use the function with the
     * onClick xml attribute.
     *
     * @param view The clicked view.
     */
    public void center(View view){
        if(map != null) map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20));
    }
}
