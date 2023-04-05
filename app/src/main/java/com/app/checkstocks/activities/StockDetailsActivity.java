package com.app.checkstocks.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.app.checkstocks.R;
import com.app.checkstocks.adapters.StockBannerAdapter;
import com.app.checkstocks.charts.BarChartManager;
import com.app.checkstocks.charts.LineChartManager;
import com.app.checkstocks.databinding.ActivityStockDetailsBinding;
import com.app.checkstocks.models.Stock;
import com.app.checkstocks.models.StockBanner;
import com.app.checkstocks.models.TimePeriod;
import com.app.checkstocks.services.StockService;
import com.app.checkstocks.charts.ChartMarkerView;
import com.app.checkstocks.utilities.DateUtils;
import com.app.checkstocks.utilities.EntryYComparator;
import com.app.checkstocks.utilities.SpeedyLinearLayoutManager;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class StockDetailsActivity extends AppCompatActivity {

    ActivityStockDetailsBinding binding;
    StockBannerAdapter stockBannerAdapter;
    ArrayList<StockBanner> stockBannerArrayList = new ArrayList<>();
    String TAG = "StockDetails";
    Stock stock;
    ArrayList<String> chartLabels = new ArrayList<>();
    StockService stockService;
    BarChartManager barChartManager;
    LineChartManager lineChartManager;

    private final List<TimePeriod> timePeriods = Arrays.asList(
            new TimePeriod(7, "dd"),
            new TimePeriod(30, "dd"),
            new TimePeriod(90, "MMM d"),
            new TimePeriod(180, "MMM d"),
            new TimePeriod(DateUtils.getYTDDays(DateUtils.getTodayDate()), "yy MMM d"),
            new TimePeriod(365, "MM/yyyy"),
            new TimePeriod(730, "MM/yyyy")
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStockDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initViews();
    }

    private void initViews() {
        // Initialize request queue and stock banner adapter
        barChartManager = new BarChartManager(binding.stockVolumeChart);
        lineChartManager = new LineChartManager(binding.stockChart, this, chartLabels);
        stockService = new StockService(this);
        stockBannerAdapter = new StockBannerAdapter(stockBannerArrayList, this, model -> {
            Stock detailStockObj = new Stock();
            detailStockObj.setName(model.getName());
            detailStockObj.setSymbol(model.getSymbol());
            detailStockObj.setLastsale(model.getLastSalePrice());
            detailStockObj.setPctchange(model.getLastSaleChange());

            Intent intent = new Intent(StockDetailsActivity.this, StockDetailsActivity.class);
            intent.putExtra("stockObj", detailStockObj);
            startActivity(intent);
        });
        binding.stockBannerRv.setLayoutManager(new SpeedyLinearLayoutManager(this, SpeedyLinearLayoutManager.HORIZONTAL, false));
        binding.stockBannerRv.setAdapter(stockBannerAdapter);

        // Get parcelable stock data and display it in the UI
        stock = getIntent().getParcelableExtra("stockObj");
        binding.stockNameTw.setText(stock.getName());
        binding.stockSymbolTw.setText(stock.getSymbol());
        binding.stockPriceTw.setText(stock.getLastsale());
        binding.stockCurrencyTw.setText(String.format("%s - USD", stock.getSymbol()));

        float pctChange = !stock.getPctchange().equals("--") ? Float.parseFloat(stock.getPctchange().replace("%", "")) : 0f;
        int color = pctChange > 0 ? R.color.light_green : R.color.light_red;
        binding.stockPctTw.setTextColor(ContextCompat.getColor(this, color));
        binding.stockPctTw.setText(String.format(Locale.getDefault(), "%.2f%%", pctChange));

        // Get slide data and set up charts
        getMarketMovers();
        setUpCharts();

        // Set up time period buttons and perform click
        setUpTimePeriodButtons();
        binding.tpLy.tpTwoTw.performClick();
    }

    private void setUpCharts() {
        //Set marker
        ChartMarkerView chartMarkerView = new ChartMarkerView(this, R.layout.custom_marker_view_layout);
        lineChartManager.setMarker(chartMarkerView);
        //Add dollar symbol to right axis
        lineChartManager.setRightAxisValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.2f$", value);
            }
        });
    }

    private void setSelectedBackgroundAndTextColor(TextView textView, boolean isSelected) {
        int selectedBackgroundColor = ContextCompat.getColor(this, R.color.white);
        int unselectedBackgroundColor = 0;
        int selectedTextColor = ContextCompat.getColor(this, R.color.white);
        int unselectedTextColor = ContextCompat.getColor(this, R.color.text_hint_color);

        textView.setBackgroundResource(isSelected ? R.drawable.bg_rounded_light : unselectedBackgroundColor);
        textView.setTextColor(isSelected ? selectedTextColor : unselectedTextColor);
    }

    private void setUpTimePeriodButtons() {
        for (int i = 0; i < timePeriods.size(); i++) {
            TimePeriod timePeriod = timePeriods.get(i);
            TextView textView = getTextPeriodView(i);
            textView.setOnClickListener(view -> handleTimePeriodButtonClick(timePeriod));
        }
    }

    private TextView getTextPeriodView(int index) {
        switch (index) {
            case 0:
                return binding.tpLy.tpOneTw;
            case 1:
                return binding.tpLy.tpTwoTw;
            case 2:
                return binding.tpLy.tpThreeTw;
            case 3:
                return binding.tpLy.tpFourTw;
            case 4:
                return binding.tpLy.tpFiveTw;
            case 5:
                return binding.tpLy.tpSixTw;
            case 6:
                return binding.tpLy.tpSevenTw;
            default:
                throw new IndexOutOfBoundsException("Invalid index for time period view");
        }
    }

    private void handleTimePeriodButtonClick(@NonNull TimePeriod timePeriod) {
        getHistoricalStockData(timePeriod.getDaysOffset());

        for (int i = 0; i < timePeriods.size(); i++) {
            TimePeriod tp = timePeriods.get(i);
            TextView textView = getTextPeriodView(i);
            setSelectedBackgroundAndTextColor(textView, tp.equals(timePeriod));
        }

        binding.stockVolumeChart.setVisibility(timePeriod.getDaysOffset() > 30 ? View.VISIBLE : View.GONE);

        String dateFormat = timePeriod.getDateFormat();
        if (dateFormat != null) {
            lineChartManager.setXAxisValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
                    if (value < chartLabels.size()) {
                        return sdf.format(Long.parseLong(chartLabels.get((int) value)));
                    } else {
                        return "0";
                    }
                }
            });
        }
    }


    private final Handler autoScrollHandler = new Handler();
    private final Runnable autoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            binding.stockBannerRv.post(() -> binding.stockBannerRv.smoothScrollToPosition(stockBannerAdapter.getItemCount() - 1));
        }
    };

    private void getHistoricalStockData(int daysOffset) {
        binding.stockChartPb.setVisibility(View.VISIBLE);
        chartLabels.clear();
        stockService.getHistoricalStockData(stock.getSymbol(), daysOffset, new StockService.StocksServiceListener() {
            @Override
            public void onDataLoaded(JSONObject data) {
                try {
                    JSONArray tradesTable = data.getJSONObject("tradesTable").getJSONArray("rows");
                    if (tradesTable.length() == 0) {
                        // Handle case where no data is available for the requested time period
                        return;
                    }
                    List<Entry> priceEntries = new ArrayList<>();
                    List<BarEntry> volumeEntries = new ArrayList<>();
                    double firstClose = 0, lastClose = 0;
                    double firstOpen = 0, lastOpen = 0;
                    int index = 0;

                    for (int i = tradesTable.length() - 1; i >= 0; i--) {
                        JSONObject trade = tradesTable.getJSONObject(i);
                        String date = trade.getString("date");
                        String closeStr = trade.getString("close");
                        String openStr = trade.getString("open");
                        String volumeStr = trade.getString("volume");

                        double close = Double.parseDouble(closeStr.replace("$", "").replace(",", ""));
                        double open = Double.parseDouble(openStr.replace("$", "").replace(",", ""));
                        double volume = Double.parseDouble(volumeStr.replace(",", ""));

                        long timestamp = DateUtils.dateToTimestamp(date);

                        chartLabels.add(String.valueOf(timestamp));
                        priceEntries.add(new Entry(index, (float) close));
                        index++;
                        if (binding.stockVolumeChart.getVisibility() == View.VISIBLE) {
                            volumeEntries.add(new BarEntry((float) index, (float) volume));
                        }

                        if (i == tradesTable.length() - 1) {
                            // If data is the oldest data, record the close price
                            firstClose = close;
                            firstOpen = open;
                        } else if (i == 0) {
                            // If data is the newest (last) data, record the close price
                            lastClose = close;
                            lastOpen = open;
                            float maxPrice = Collections.max(priceEntries, new EntryYComparator()).getY();
                            float minPrice = Collections.min(priceEntries, new EntryYComparator()).getY();
                            Log.e(TAG, "maxPrice: " + maxPrice);
                            Log.e(TAG, "minPrice: " + minPrice);

                            String lowStr = trade.getString("low");
                            String highStr = trade.getString("high");

                            double high = Double.parseDouble(highStr.replace("$", "").replace(",", ""));
                            double low = Double.parseDouble(lowStr.replace("$", "").replace(",", ""));

                            // Set text color based on whether stock is up or down
                            boolean isUp = lastClose > firstClose;
                            binding.openTw.setTextColor(open > close ? Color.GREEN : Color.RED);
                            binding.closeTw.setTextColor(close > open ? Color.GREEN : Color.RED);
                            binding.highTw.setTextColor(high > low ? Color.GREEN : Color.RED);
                            binding.lowTw.setTextColor(low > high ? Color.GREEN : Color.RED);

                            // Parse UI
                            binding.openTw.setText(String.format(Locale.getDefault(), "$%.2f", open));
                            binding.highTw.setText(String.format(Locale.getDefault(), "$%.2f", high));
                            binding.lowTw.setText(String.format(Locale.getDefault(), "$%.2f", low));
                            binding.closeTw.setText(String.format(Locale.getDefault(), "$%.2f", close));

                            // Set the stock chart data
                            setChartData(priceEntries, volumeEntries, isUp);
                        }
                    }
                } catch (JSONException | ParseException e) {
                    throw new RuntimeException(e);
                }

            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "onError: " + error.getMessage());
            }
        });
    }

    private void getHistoricalStockBannerData(@NonNull StockBanner stockBanner) {
        int daysOffset = 30;
        stockService.getHistoricalStockData(stockBanner.getSymbol(), daysOffset, new StockService.StocksServiceListener() {
            @Override
            public void onDataLoaded(JSONObject data) {
                try {
                    JSONArray tradesTable = data.getJSONObject("tradesTable").getJSONArray("rows");
                    int index = 0;
                    // Process the historical prices (Get data from start to end)
                    for (int i = tradesTable.length() - 1; i >= 0; i--) {
                        JSONObject trade = tradesTable.getJSONObject(i);
                        String date = trade.getString("date");
                        String closeStr = trade.getString("close");
                        double close = Double.parseDouble(closeStr.replace("$", "").replace(",", ""));
                        // Fill the entry to the chart data
                        stockBanner.getStockEntries().add(new Entry(index, (float) close));
                        index++;
                        autoScrollHandler.post(autoScrollRunnable);
                    }
                    //Insert data
                    stockBannerAdapter.addData(stockBanner);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "onError: " + error.getMessage());
            }
        });
    }

    private void getMarketMovers() {
        stockService.getMarketMovers(new StockService.MarketMoversCallback() {
            @Override
            public void onSuccess(JSONObject mostActiveByShareVolume, JSONObject mostAdvanced, JSONObject mostDeclined, JSONObject mostActiveByDollarVolume, JSONObject nasdaq100Movers) {
                try {
                    String dataAsOf = nasdaq100Movers.getString("dataAsOf");
                    String lastTradeTimestamp = nasdaq100Movers.getString("lastTradeTimestamp");
                    JSONArray stocks = nasdaq100Movers.getJSONObject("table").getJSONArray("rows");
                    for (int i = 0; i < stocks.length(); i++) {
                        JSONObject obj = stocks.getJSONObject(i);
                        String symbol = obj.getString("symbol");
                        String name = obj.getString("name");
                        String lastSalePrice = obj.getString("lastSalePrice");
                        String lastSaleChange = obj.getString("lastSaleChange");
                        String change = obj.getString("change");
                        String deltaIndicator = obj.getString("deltaIndicator");
                        //Pass empty stockEntry for now
                        List<Entry> stockEntry = new ArrayList<>();
                        StockBanner stockBanner = new StockBanner(symbol, name, lastSalePrice, lastSaleChange, change, deltaIndicator, stockEntry);
                        getHistoricalStockBannerData(stockBanner);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "onError: " + e.getMessage());
            }
        });

    }

    public void setChartData(List<Entry> stockEntries, List<BarEntry> stockVolumeEntries, boolean isUp) {
        //stock chart
        // Create a gradient drawable to use as the fill
        int[] colorsGradient = isUp ? new int[]{Color.GREEN, Color.TRANSPARENT} : new int[]{Color.RED, Color.TRANSPARENT};
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colorsGradient);
        // Set the shape to rectangle and the size to match the chart dimensions
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setSize(binding.stockChart.getWidth(), binding.stockChart.getHeight());
        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        lineChartManager.setData(stockEntries, true);
        lineChartManager.setDataSetColors(isUp ? Color.GREEN : Color.RED);
        // Set the fill drawable for the data set
        lineChartManager.setDataSetFillDrawable(gradientDrawable);
        lineChartManager.setDataSetLineWidth(2f);
        lineChartManager.animate(500, Easing.EaseInOutCubic);
        //stock volume chart
        barChartManager.setData(stockVolumeEntries, this);
        barChartManager.animate(500, Easing.EaseInOutCubic);
        binding.stockChartPb.setVisibility(View.GONE); //Set chart progress bar visibility gone
    }

}