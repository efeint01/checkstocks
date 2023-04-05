package com.app.checkstocks.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.app.checkstocks.R;
import com.app.checkstocks.charts.LineChartManager;
import com.app.checkstocks.databinding.RowStockBannerItemBinding;
import com.app.checkstocks.models.StockBanner;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StockBannerAdapter extends RecyclerView.Adapter<StockBannerAdapter.ViewHolder> {

    final ArrayList<StockBanner> arrayList;
    final Context context;
    final NumberFormat currencyFormat;
    final StockBannerListener stockBannerListener;
    LineChartManager lineChartManager;

    public StockBannerAdapter(ArrayList<StockBanner> arrayList, Context context, StockBannerListener stockBannerListener) {
        this.arrayList = arrayList;
        this.context = context;
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
        this.stockBannerListener = stockBannerListener;
    }

    @NonNull
    @Override
    public StockBannerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StockBannerAdapter.ViewHolder(RowStockBannerItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull StockBannerAdapter.ViewHolder holder, int position) {
        StockBanner model = arrayList.get(position);
        holder.binding.stockSymbolTw.setText(model.getSymbol());
        // Set the stock price text
        float price = Float.parseFloat(model.getLastSalePrice().replace("$", "").replace(",", ""));
        float pctChange = Float.parseFloat(model.getLastSaleChange().replace("+","").replace(",",""));
        String formattedPctChange = String.format(Locale.getDefault(),"%.2f%%", pctChange);

        holder.binding.stockPriceTw.setText(currencyFormat.format(price));
        holder.binding.stockPctChangeTw.setText(formattedPctChange);
        // update chart data and text color
        lineChartManager = new LineChartManager(holder.binding.lineChart,context,null);
        lineChartManager.setLeftAxisGridlines(false);
        lineChartManager.setRightAxisGridlines(false);
        lineChartManager.setXAxisGridlines(false);
        lineChartManager.setTouchEnabled(false);
        updateChartAndText(model.getStockEntries(), holder.binding.lineChart, model.getDeltaIndicator(), holder.binding.stockPriceTw, holder.binding.stockPctChangeTw);

        holder.binding.getRoot().setOnClickListener(view -> {
            stockBannerListener.onClickListener(model);
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public void addData(StockBanner newData) {
        arrayList.add(newData);
        notifyItemInserted(arrayList.size() - 1);
    }

    public void updateChartAndText(List<Entry> entries, @NonNull LineChart lineChart, @NonNull String delta, TextView priceTw, TextView pctChangeTw) {
        lineChartManager.setData(entries,false);
        // Create a LineDataSet object with your data and label
        LineDataSet dataSet = new LineDataSet(entries, null);
        // Remove dots from the line
        dataSet.setDrawCircles(false);
        int[] colorsGradient = new int[0];
        // Set the color of the line based on whether the stock is up or down or unchanged
        switch (delta) {
            case "up":
                lineChartManager.setDataSetColors(Color.GREEN);
                colorsGradient = new int[]{Color.GREEN, Color.TRANSPARENT};
                priceTw.setTextColor(Color.GREEN);
                pctChangeTw.setTextColor(Color.GREEN);
                break;
            case "down":
                lineChartManager.setDataSetColors(Color.RED);
                colorsGradient = new int[]{Color.RED, Color.TRANSPARENT};
                priceTw.setTextColor(Color.RED);
                pctChangeTw.setTextColor(Color.RED);
                break;
            case "unch": //unchanged
                lineChartManager.setDataSetColors(Color.GRAY);
                colorsGradient = new int[]{Color.GRAY, Color.TRANSPARENT};
                priceTw.setTextColor(ContextCompat.getColor(context, R.color.gray));
                pctChangeTw.setTextColor(ContextCompat.getColor(context, R.color.gray));
                pctChangeTw.setText("0.00");
                break;
        }
        // Enable filling for the line
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colorsGradient);
        // Set the shape to rectangle and the size to match the chart dimensions
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setSize(lineChart.getWidth(), lineChart.getHeight());
        // Set the alpha value to create a semi-transparent effect
        gradientDrawable.setAlpha(200);
        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        lineChartManager.setDataSetFillDrawable(gradientDrawable);
        lineChartManager.animate(500, Easing.EaseInOutCubic);

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RowStockBannerItemBinding binding;
        public ViewHolder(@NonNull RowStockBannerItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            // Chart Style
        }
    }

    public interface StockBannerListener {
        void onClickListener(StockBanner model);
    }
}
