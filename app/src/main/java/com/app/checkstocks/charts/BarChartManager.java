package com.app.checkstocks.charts;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.app.checkstocks.R;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.List;

public class BarChartManager {

    private final BarChart chart;

    public BarChartManager(BarChart chart) {
        this.chart = chart;

        // Set up the chart
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setDrawBorders(false);
        chart.setDescription(null);

        // Set up the X-axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawLabels(false);

        // Set up the left Y-axis
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawAxisLine(false);
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawLabels(false);

        // Set up the right Y-axis
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawAxisLine(false);
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawLabels(false);
    }

    public void setData(List<BarEntry> entries, Context context) {
        BarDataSet dataSet = new BarDataSet(entries, null);
        dataSet.setDrawValues(false);
        dataSet.setColor(ContextCompat.getColor(context, R.color.light));
        dataSet.setDrawIcons(false);

        BarData barData = new BarData(dataSet);
        chart.setData(barData);
        chart.invalidate();
    }

    public void animate(int duration, Easing.EasingFunction easing) {
        chart.animateY(duration, easing);
        chart.invalidate();
    }

    public void clearData() {
        chart.clear();
        chart.invalidate();
    }

    public void setMarkerView(MarkerView markerView) {
        chart.setMarker(markerView);
    }
}
