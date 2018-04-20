package com.commeto.kuleuven.commetov2.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.commeto.kuleuven.commetov2.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.LinkedList;

/**
 * Created by Jonas on 15/03/2018.
 */

public class VibrationFragment extends Fragment {

    private GraphView graphView;
    private LinkedList<Double> points;
    private LineGraphSeries<DataPoint> series;
    private int x;

    @Override
    public void onCreate(Bundle bundle){

        points = new LinkedList<>();

        series = new LineGraphSeries<>();
        series.setColor(getResources().getColor(R.color.red));
        series.setThickness(5);
        series.setBackgroundColor(getResources().getColor(R.color.border_grey));
        x = 0;

        super.onCreate(bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_vibration, container, false);

        graphView = (GraphView) root.findViewById(R.id.graph);
        graphView.getViewport().setScrollable(false);
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMinY(0);
        graphView.getViewport().setMaxY(50);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(60);

        graphView.addSeries(series);

        return root;
    }

//==================================================================================================
    //public methods

    public void append(double vibration){

        if(x >= 60) points.removeFirst();
        if(vibration > 50) points.addLast(50.0);
        else points.addLast(vibration);

        DataPoint[] dataPoints = new DataPoint[points.size()];
        for(int i = 0; i < points.size(); i++){
            dataPoints[i] = new DataPoint(i, points.get(i));
        }

        series.resetData(dataPoints);

        if(x >= 60){
            graphView.getViewport().setMinX(x - 60);
            graphView.getViewport().setMaxX(x);
        } else{
            x++;
        }
    }
}
