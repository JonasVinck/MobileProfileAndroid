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
 * Created by Jonas on 10/04/2018.
 */

public class MeasuringGraphFragment extends Fragment{

    private LinkedList<Double> points;
    private GraphView graphView;
    private LineGraphSeries<DataPoint> series;
    private int x;
    private boolean limited;

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

        graphView = view.findViewById(R.id.graph);
        graphView.getViewport().setScrollable(false);
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(60);
        graphView.getGridLabelRenderer().setGridColor(getResources().getColor(R.color.white));
        if(limited) {
            graphView.getViewport().setYAxisBoundsManual(true);
            graphView.getViewport().setMinY(0);
            graphView.getViewport().setMaxY(80);
            graphView.getGridLabelRenderer().setVerticalLabelsVisible(false);
        } else{
            graphView.getViewport().setYAxisBoundsManual(false);
        }
        graphView.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graphView.addSeries(series);

        return view;
    }

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
