package com.commeto.kuleuven.commetov2.support;

import android.content.Context;
import android.graphics.Color;

import com.commeto.kuleuven.commetov2.sqlSupport.LocalRoute;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.services.commons.geojson.Feature;
import com.mapbox.services.commons.geojson.FeatureCollection;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static com.commeto.kuleuven.commetov2.http.HTTPStatic.convertInputStreamToString;
import static com.commeto.kuleuven.commetov2.support.Static.makeToastLong;

/**
 * Created by Jonas on 3/04/2018.
 */

public class MapSupport {
//==================================================================================================
    // clas specs

    private LatLngBounds bounds;

    private Context context;
    private LocalRoute localRoute;
    private JSONArray start, end;

    public MapSupport(Context context, LocalRoute localRoute){
        this.context = context;
        this.localRoute = localRoute;
    }
//==================================================================================================
    //public functions

    public void displayGeoJson(MapboxMap map){

        InputStream stream;
        String geoJsonString;

        try {
            stream = InternalIO.getInputStream(context, localRoute.getLocalId() + ".json");
            geoJsonString = rideLocalGeoJson(stream);
            try {
                JSONObject geojson = new JSONObject(geoJsonString);

                GeoJsonSource source = new GeoJsonSource("geojson", geoJsonString);
                map.addSource(source);
                LineLayer lineLayer = new LineLayer("lines", "geojson");
            lineLayer.setProperties(
                    PropertyFactory.lineDasharray(new Float[]{0.01f, 2f}),
                    PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                    PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                    PropertyFactory.lineWidth(5f),
                    PropertyFactory.lineColor(Color.parseColor("#e55e5e"))
            );
            map.addLayer(lineLayer);
            } catch (Exception e){}
        } catch (IOException e){
            try {
                stream = InternalIO.getInputStream(context, localRoute.getLocalId() + "_server.json");
            } catch (IOException e2){
                makeToastLong(context, "Er ging iets mis met het inlezen van de route");
            }
        }
    }

    public void displayRide(MapboxMap map){
        ArrayList<Position> array = generateRoute();
        LineString lineString = LineString.fromCoordinates(array);
        FeatureCollection collection = FeatureCollection.fromFeatures(new Feature[]{Feature.fromGeometry(lineString)});
        Source source = new GeoJsonSource("line-source", collection);
        map.addSource(source);

        LineLayer layer = new LineLayer("linelayer", "line-source");
        layer.setProperties(
                PropertyFactory.lineWidth(5f),
                PropertyFactory.lineColor(Color.parseColor("#37A3F7"))
        );

        if(array != null) {
            LatLng beginning = new LatLng(
                    array.get(0).getLatitude(),
                    array.get(0).getLongitude()
            );
            LatLng end = new LatLng(
                    array.get(array.size() - 1).getLatitude(),
                    array.get(array.size() - 1).getLongitude()
            );

            map.addMarker(new MarkerOptions()
                    .setPosition(beginning)
                    .setTitle("Begin")
            );
            map.addMarker(new MarkerOptions()
                    .setPosition(end)
                    .setTitle("Einde")
            );
        }
        map.addLayer(layer);
    }

    public LatLngBounds getBounds(){
        return this.bounds;
    }
//==================================================================================================
    //private functions

    private String rideLocalGeoJson(InputStream stream){

        JSONObject rideGeoJson = new JSONObject();

        try {
            JSONObject ride = new JSONObject(convertInputStreamToString(stream));
            double lat, lon;
            double maxLat = 0, maxLon = 0, minLat = Double.MAX_VALUE, minLon = Double.MAX_VALUE;
            JSONArray array = ride.getJSONArray("measurements");
            JSONArray coords = new JSONArray(), temp;
            for(int i = 0; i < array.length(); i++){
                lat = array.getJSONObject(i).getDouble("lat");
                lon = array.getJSONObject(i).getDouble("lon");
                temp = new JSONArray();
                temp.put(lat).put(lon);
                coords.put(temp);
                if(lat > maxLat) maxLat = lat;
                if(lon > maxLon) maxLon = lon;
                if(lat < minLat) minLat = lat;
                if(lon < minLon) minLon = lon;
            }
            bounds = LatLngBounds.from(
                    maxLat, maxLon,
                    minLat, minLon
            );

            start = new JSONArray();
            start.put(array.getJSONObject(0).getDouble("lat"));
            start.put(array.getJSONObject(0).getDouble("lon"));
            end = new JSONArray();
            end.put(array.getJSONObject(0).getDouble("lat"));
            end.put(array.getJSONObject(0).getDouble("lon"));

            int id = context.getResources().getIdentifier(
                    "line_layer","raw", context.getPackageName()
            );
            InputStream inputStream = context.getResources().openRawResource(id);
            rideGeoJson = new JSONObject(convertInputStreamToString(inputStream));
            rideGeoJson
                    .getJSONObject("source")
                    .getJSONObject("data")
                    .getJSONObject("geometry")
                    .put("coordinates", coords);
        } catch (IOException e){
            makeToastLong(context, "Er ging iets met het lezen van de rit.");
        } catch (JSONException e){
            makeToastLong(context, "Er ging iets mis met het omzetten van de rit.");
        }

        return rideGeoJson.toString();
    }

    private ArrayList<Position> generateRoute(){

        ArrayList<Position> route;

        InputStream stream;
        try{
            stream = InternalIO.getInputStream(context, localRoute.getLocalId() + "_snapped.json");
            route = getServerRoute(stream);
        }catch (IOException e){
            try{
                stream = InternalIO.getInputStream(context, localRoute.getLocalId() + "_unsnapped.json");
                route = getServerRoute(stream);
            }catch (IOException e2) {
                try {
                    stream = InternalIO.getInputStream(context, localRoute.getLocalId() + ".json");
                    route = getLocalRoute(stream);
                } catch (IOException e3) {
                    makeToastLong(context, "Kan route niet weergeven");
                    return null;
                }
            }
        }

        return route;
    }

    private ArrayList<Position> getServerRoute(InputStream stream){

        JSONArray local;
        ArrayList<Position> route;
        double lat, lon;
        try{
            local = new JSONArray(convertInputStreamToString(stream));
            route = new ArrayList<>();
            double maxLat = 0, maxLon = 0, minLat = Double.MAX_VALUE, minLon = Double.MAX_VALUE;
            for(int i = 0; i < local.length(); i++){
                lat = local.getJSONArray(i).getDouble(1);
                lon = local.getJSONArray(i).getDouble(0);
                route.add(Position.fromCoordinates(
                        lon,
                        lat
                ));
                if(lat > maxLat) maxLat = lat;
                if(lon > maxLon) maxLon = lon;
                if(lat < minLat) minLat = lat;
                if(lon < minLon) minLon = lon;
            }
            bounds = LatLngBounds.from(
                    maxLat, maxLon,
                    minLat, minLon
            );
            return route;
        } catch (Exception e){
            makeToastLong(context, "Er ging iets mis bij het opstellen.");
        }

        return null;
    }

    private ArrayList<Position> getLocalRoute(InputStream stream){

        JSONObject local;
        ArrayList<Position> route;
        try{
            local = new JSONObject(convertInputStreamToString(stream));
            route = new ArrayList<>();
            double lat, lon;
            double maxLat = 0, maxLon = 0, minLat = Double.MAX_VALUE, minLon = Double.MAX_VALUE;
            JSONArray array = local.getJSONArray("measurements");
            for(int i = 0; i < array.length(); i++){
                lon = array.getJSONObject(i).getDouble("lon");
                lat = array.getJSONObject(i).getDouble("lat");
                route.add(Position.fromCoordinates(
                        lon,
                        lat,
                        array.getJSONObject(i).getDouble("ele")
                ));
                if(lat > maxLat) maxLat = lat;
                if(lon > maxLon) maxLon = lon;
                if(lat < minLat) minLat = lat;
                if(lon < minLon) minLon = lon;
            }
            bounds = LatLngBounds.from(
                    maxLat, maxLon,
                    minLat, minLon
            );
            return route;
        } catch (Exception e){
            makeToastLong(context, "Er ging iets mis bij het opstellen.");
        }

        return null;
    }
}
