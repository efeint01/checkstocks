package com.app.checkstocks.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.checkstocks.R;
import com.app.checkstocks.charts.LineChartManager;
import com.app.checkstocks.databinding.RowStockItemBinding;
import com.app.checkstocks.models.Stock;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.ViewHolder> {

    ArrayList<Stock> arrayList;
    final NumberFormat currencyFormat;
    int maxStockNameLength = 30;

    final StockListener stockListener;
    LineChartManager lineChartManager;

    public StockAdapter(ArrayList<Stock> arrayList, StockListener stockListener) {
        this.arrayList = arrayList;
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
        this.stockListener = stockListener;
    }

    @NonNull
    @Override
    public StockAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StockAdapter.ViewHolder(RowStockItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull StockAdapter.ViewHolder holder, int position) {
        // Get the stock model for the current position
        Stock model = arrayList.get(position);

        // Set the stock name text to the full name if it is shorter than the maximum length,
        // or shorten it and add an ellipsis if it is longer
        String reducedStockName = model.getName().substring(0, Math.min(model.getName().length(), maxStockNameLength)) + "...";
        holder.binding.stockNameTw.setText(model.getName().length() > maxStockNameLength ? reducedStockName : model.getName());

        // Set the stock symbol text
        holder.binding.stockSymbolTw.setText(model.getSymbol());

        lineChartManager = new LineChartManager(holder.binding.lineChart,holder.binding.getRoot().getContext(),null);
        lineChartManager.setLeftAxisGridlines(false);
        lineChartManager.setRightAxisGridlines(false);
        lineChartManager.setXAxisGridlines(false);
        lineChartManager.setTouchEnabled(false);

        // Set the percentage change text and background color if the percentage change is not "--"
        if (!model.getPctchange().equals("--")) {
            float pctChange = Float.parseFloat(model.getPctchange().replace("%", ""));
            holder.binding.stockPctChangeTw.setText(String.format(Locale.getDefault(), "%.2f%%", pctChange));
            holder.binding.stockPctChangeTw.setBackgroundResource(pctChange > 0 ? R.drawable.bg_rounded_green : R.drawable.bg_rounded_red);
            setData(model.getStockEntries(), holder.binding.lineChart, pctChange > 0);
        } else {
            // If the percentage change is "--", set the text to that and do not show the chart
            holder.binding.stockPctChangeTw.setText(model.getPctchange());
            setData(model.getStockEntries(), holder.binding.lineChart, false);
        }

        // Set the stock price text
        float price = Float.parseFloat(model.getLastsale().replace("$", "").replace(",", ""));
        holder.binding.stockPriceTw.setText(currencyFormat.format(price));

        // Set a click listener for the list item
        holder.binding.getRoot().setOnClickListener(view -> {
            // Handle the click event
            stockListener.onClickListener(model);
        });

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public void addAllData(ArrayList<Stock> newData, int startPosition) {
        arrayList.addAll(newData);
        notifyItemRangeInserted(startPosition, newData.size());
    }

    public void setStocks(ArrayList<Stock> arrayList) {
        this.arrayList = arrayList;
        notifyDataSetChanged();
    }

    public void setStock(Stock stock) {
        this.arrayList.clear();
        this.arrayList.add(stock);
        notifyDataSetChanged();
    }
    public void addData(Stock newData) {
        arrayList.add(newData);
        notifyItemInserted(arrayList.size() - 1);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RowStockItemBinding binding;

        public ViewHolder(@NonNull RowStockItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public void setData(List<Entry> entries, @NonNull LineChart lineChart, boolean isUp) {
        lineChartManager.setData(entries,false);
        // Create a gradient drawable to use as the fill
        int[] colorsGradient = isUp ? new int[]{Color.GREEN, Color.TRANSPARENT} : new int[]{Color.RED, Color.TRANSPARENT};
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colorsGradient);
        // Set the shape to rectangle and the size to match the chart dimensions
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setSize(lineChart.getWidth(), lineChart.getHeight());
        // Set the alpha value to create a semi-transparent effect
        gradientDrawable.setAlpha(200);
        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        lineChartManager.setDataSetColors(isUp ? Color.GREEN : Color.RED);
        lineChartManager.setDataSetFillDrawable(gradientDrawable);
        lineChartManager.setDataSetLineWidth(1f);
        lineChartManager.animate(500, Easing.EaseInOutCubic);
    }

    public interface StockListener {
        void onClickListener(Stock model);
    }

}
