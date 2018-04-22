package com.commeto.kuleuven.MP.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.commeto.kuleuven.MP.interfaces.MapStyleInterface;
import com.commeto.kuleuven.MP.R;

/**
 * Created by Jonas on 17/04/2018.
 */

public class MapOptionAdapter extends ArrayAdapter<String>{

    private class ViewHolder {
        private TextView title;
        private TextView description;
    }

    private MapStyleInterface mapStyleInterface;
    private Context context;
    private String[] descriptions;

    public MapOptionAdapter(Context context, String[] titles, String[] descriptions, MapStyleInterface mapStyleInterface){
        super(context, 0, titles);
        this.context = context;
        this.descriptions = descriptions;
        this.mapStyleInterface = mapStyleInterface;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if(view == null){
            view = LayoutInflater.from(this.context).inflate(R.layout.map_options_list_view, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.title = view.findViewById(R.id.title);
            viewHolder.description = view.findViewById(R.id.description);
            view.setTag(viewHolder);
        }

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.title.setText(this.getItem(position));
        viewHolder.description.setText(descriptions[position]);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapStyleInterface.setStyle(MapOptionAdapter.this.getItem(position));
            }
        });

        return view;
    }
}
