package com.commeto.kuleuven.MP.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.commeto.kuleuven.MP.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

/**
 * <pre>
 * Created by Jonas on 10/04/2018.
 *
 * Fragment to display graph while riding. Fragment only displays 60 values to lower burden.
 * </pre>
 */

public class MeasuringGraphFragment extends Fragment{

    private LinkedList<Double> points;
    private LineGraphSeries<DataPoint> series;
    private int x;
    private boolean limited;

    /**
     * Get a new instances of the MeasuringGraphFragment. If the MeasuringGraphFragment is limited
     * the vertical axis will not rescale if the value exceeds 80.
     *
     * @param  limited Boolean representing whether the graph is limited or not.
     * @return A new MeasuringGraphFragment.
     */
    public static MeasuringGraphFragment newInstance(boolean limited) {
        MeasuringGraphFragment fragment = new MeasuringGraphFragment();
        Bundle args = new Bundle();
        args.putBoolean("limit", limited);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            limited = getArguments().getBoolean("limit", true);

            points = new LinkedList<>();

            series = new LineGraphSeries<>();
            series.setColor(getResources().getColor(R.color.accent));
            series.setThickness(5);
            x = 0;
        }
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graph, container, false);

        //Setting the GraphView.
        GraphView graphView = view.findViewById(R.id.graph);
        graphView.getViewport().setScrollable(false);
        //X limit always 0-60 and not re-scalable.
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(60);
        graphView.getGridLabelRenderer().setGridColor(getResources().getColor(R.color.white));

        if(limited) {
            /*
            if limited:
                - Vertical rescaling disabled.
                - Vertical limit set to 0 - 80.
                - Axis labels disabled on vertical axis.
             */
            graphView.getViewport().setYAxisBoundsManual(true);
            graphView.getViewport().setMinY(0);
            graphView.getViewport().setMaxY(80);
            graphView.getGridLabelRenderer().setVerticalLabelsVisible(false);
        } else{
            //If not limited, vertical axis can scale according to given data.
            graphView.getViewport().setYAxisBoundsManual(false);
        }
        graphView.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graphView.addSeries(series);

        return view;
    }

    /**
     * <pre>
     * Method used to add data to the graph.
     *
     * When more than 60 points are in the points array, the first one is removed.
     * </pre>
     * @param point
     */

    public void append(double point){
        if(x >= 60) points.removeFirst();
        points.addLast(point);

        DataPoint[] dataPoints = new DataPoint[points.size()];
        for(int i = 0; i < points.size(); i++){
            dataPoints[i] = new DataPoint(i, points.get(i));
        }

        series.resetData(dataPoints);

        if(x < 60) x++;

    }
}
