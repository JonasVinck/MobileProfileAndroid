package com.commeto.kuleuven.MP.geoCoding;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filterable;
import android.widget.Filter;
import android.widget.TextView;

import com.commeto.kuleuven.MP.R;
import com.commeto.kuleuven.MP.support.InternalIO;
import retrofit.Response;
import com.mapbox.geocoder.GeocoderCriteria;
import com.mapbox.geocoder.MapboxGeocoder;
import com.mapbox.geocoder.service.models.GeocoderFeature;
import com.mapbox.geocoder.service.models.GeocoderResponse;

import java.io.IOException;
import java.util.List;

/**
 * Created by Jonas on 15/04/2018.
 *
 * Adapter used to display geocoding options.
 */

public class GeoCodeAdapter extends BaseAdapter implements Filterable{

    private Context context;
    private GeoCodeFilter geoCodeFilter;
    private List<GeocoderFeature> features;

    public GeoCodeAdapter(Context context){
        this.context = context;
    }

    @Override
    public int getCount() {
        return features.size();
    }

    @Override
    public GeocoderFeature getItem(int position) {
        return features.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get view
        View view;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
        } else {
            view = convertView;
        }

        TextView text = (TextView) view;

        GeocoderFeature feature = getItem(position);
        text.setText(feature.getPlaceName());

        return view;
    }

    @Override
    public Filter getFilter() {
        if (geoCodeFilter == null) {
            geoCodeFilter = new GeoCodeFilter();
        }
        return geoCodeFilter;
    }

    private class GeoCodeFilter extends Filter{

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            // No constraint
            if (TextUtils.isEmpty(constraint)) {
                return results;
            }

            // The geocoder client
            MapboxGeocoder client = new MapboxGeocoder.Builder()
                    .setAccessToken(context.getString(R.string.key))
                    .setLocation(constraint.toString())
                    .setType(GeocoderCriteria.TYPE_ADDRESS)
                    .build();

            Response<GeocoderResponse> response;
            try {
                response = client.execute();
            } catch (IOException e) {
                InternalIO.writeToLog(context, e);
                return results;
            }

            features = response.body().getFeatures();
            results.values = features;
            results.count = features.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results != null && results.count > 0) {
                features = (List<GeocoderFeature>) results.values;
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
