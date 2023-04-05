package com.app.checkstocks.charts;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class LineChartManager {
    private final LineChart chart;
    private LineDataSet dataSet;

    public LineChartManager(LineChart chart, Context context, ArrayList<String> chartLabels) {
        this.chart = chart;

        // Set up the chart
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            chart.setDefaultFocusHighlightEnabled(false);
        }

        chart.setTouchEnabled(true);
        chart.getLegend().setEnabled(false); // Disable legend
        chart.setDrawBorders(false); // Disable chart borders
        chart.setDescription(null); // Remove chart description
        chart.setNoDataText(null);

        // Set up the X-axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(true);
        xAxis.setDrawLabels(chartLabels != null);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // Calculate the maximum width of the labels
        if (chartLabels != null) {
            float maxWidth = 0f;
            Paint paint = new Paint();
            paint.setTextSize(xAxis.getTextSize());
            for (String label : chartLabels) {
                float width = paint.measureText(label);
                if (width > maxWidth) {
                    maxWidth = width;
                }
            }

            // Set the text size of the X-axis labels dynamically based on the maximum width
            float textSize = xAxis.getTextSize();
            if (maxWidth > 0f && maxWidth > chart.getWidth() / chartLabels.size()) {
                textSize *= chart.getWidth() / chartLabels.size() / maxWidth;
                xAxis.setTextSize(textSize);
                // Set the minimum text size to 12dp
                float minimumTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, context.getResources().getDisplayMetrics());
                xAxis.setTextSize(minimumTextSize);
            }
        }

        // Set up the left Y-axis
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawAxisLine(false);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawLabels(false);

        // Set up the right Y-axis
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawAxisLine(false);
        rightAxis.setDrawGridLines(true);
        rightAxis.setDrawLabels(chartLabels != null);
        rightAxis.setTextColor(Color.WHITE);
    }

    public void setXAxisGridlines(boolean isEnabled) {
        chart.getXAxis().setDrawGridLines(isEnabled);
    }

    public void setRightAxisGridlines(boolean isEnabled) {
        chart.getAxisRight().setDrawGridLines(isEnabled);
    }

    public void setLeftAxisGridlines(boolean isEnabled) {
        chart.getAxisLeft().setDrawGridLines(isEnabled);
    }

    public void setTouchEnabled(boolean isEnabled) {
        chart.setTouchEnabled(isEnabled);
    }

    public void setRightAxisValueFormatter(ValueFormatter valueFormatter) {
        chart.getAxisRight().setValueFormatter(valueFormatter);
    }

    public void setXAxisValueFormatter(ValueFormatter valueFormatter) {
        chart.getXAxis().setValueFormatter(valueFormatter);
    }

    public void setMarker(MarkerView markerView) {
        chart.setMarker(markerView);
    }

    public void clearData() {
        chart.clear();
        chart.invalidate();
    }

    public void animate(int duration, Easing.EasingFunction easing) {
        chart.animateY(duration, easing);
        chart.invalidate();
    }

    public void setDataSetColors(int... colors) {
        dataSet.setColors(colors);
    }

    public void setDataSetLineWidth(float width) {
        dataSet.setLineWidth(width);
    }

    public void setDataSetFillDrawable(Drawable drawable) {
        dataSet.setDrawFilled(true);
        dataSet.setFillDrawable(drawable);
    }

    public void setData(List<Entry> entries, boolean isCurved) {
        if (entries == null || entries.isEmpty()) {
            // Handle null or empty entries
            return;
        }
        dataSet = new LineDataSet(entries, null);
        dataSet.setHighLightColor(Color.WHITE);
        dataSet.setHighlightEnabled(true);
        // Adjust the line smoothing
        dataSet.setMode(isCurved ? LineDataSet.Mode.CUBIC_BEZIER : LineDataSet.Mode.LINEAR);
        // Adjust the line width
        dataSet.setLineWidth(1f);
        // Remove dots from the line
        dataSet.setDrawCircles(false);
        // Create a LineData object with the LineDataSet
        LineData lineData = new LineData(dataSet);
        // Hide the value text for the data set
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return ""; // Return an empty string to hide the value text
            }
        });

        // Set the LineData object to the chart
        chart.setData(lineData);
        chart.invalidate(); // Refresh the chart to apply the style changes

    }

}
