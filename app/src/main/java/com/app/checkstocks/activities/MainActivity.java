package com.app.checkstocks.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.splashscreen.SplashScreen;
import androidx.recyclerview.widget.RecyclerView;

import com.app.checkstocks.R;
import com.app.checkstocks.adapters.StockAdapter;
import com.app.checkstocks.databinding.ActivityMainBinding;
import com.app.checkstocks.databinding.EmptyViewLyBinding;
import com.app.checkstocks.models.Stock;
import com.app.checkstocks.services.StockService;
import com.app.checkstocks.utilities.DateUtils;
import com.app.checkstocks.utilities.RVEmptyObserver;
import com.github.mikephil.charting.data.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    StockAdapter stockAdapter;
    ArrayList<Stock> stockArrayList = new ArrayList<>();

    boolean disableSplash, isDataLoading;
    SplashScreen splashScreen;
    String TAG = "MainActivity";
    int dataLimit = 100;
    int dataOffset;
    StockService stockService;
    EmptyViewLyBinding emptyViewLyBinding;
    RVEmptyObserver emptyObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Handle the splash screen transition.
        splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (disableSplash) {
                    view.getViewTreeObserver().removeOnPreDrawListener(this);
                }
                return false;
            }
        });

        initViews();

    }

    RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        private boolean userScrolledToEnd = false;

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE && userScrolledToEnd) {
                // Load more data
                binding.loadDataPb.setVisibility(View.VISIBLE);
                isDataLoading = true; //set data loading true
                userScrolledToEnd = false;
                dataOffset += dataLimit; //update data offset
                getStocks();

            }
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            // Check if the user has scrolled to the end of the list
            userScrolledToEnd = !recyclerView.canScrollVertically(1);
        }
    };

    private void initViews() {
        //init stockService
        stockService = new StockService(this);
        //init adapter & recyclerview
        stockAdapter = new StockAdapter(stockArrayList, model -> {
            Intent intent = new Intent(MainActivity.this, StockDetailsActivity.class);
            intent.putExtra("stockObj", model);
            startActivity(intent);
        });
        //create no data layout and add to layout
        emptyViewLyBinding = EmptyViewLyBinding.inflate(LayoutInflater.from(this), binding.getRoot(), false);
        binding.recyclerLy.addView(emptyViewLyBinding.getRoot());
        //create observer and connect to adapter
        emptyObserver = new RVEmptyObserver(binding.recyclerView, emptyViewLyBinding.getRoot());
        stockAdapter.registerAdapterDataObserver(emptyObserver);
        binding.recyclerView.setAdapter(stockAdapter);
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)) {
                    //Search stocks by symbol
                    stockService.getSearchStockBySymbol(query.toUpperCase(), new StockService.StocksServiceListener() {
                        @Override
                        public void onDataLoaded(JSONObject data) {
                            try {
                                if (!data.isNull("data")) {
                                    Log.e(TAG, "onDataLoaded: " + data);
                                    JSONObject stockObj = data.getJSONObject("data");
                                    String symbol = stockObj.getString("symbol");
                                    String companyName = stockObj.getString("companyName");
                                    JSONObject primaryData = stockObj.getJSONObject("primaryData");
                                    String lastSalePrice = primaryData.getString("lastSalePrice");
                                    String netChange = primaryData.getString("netChange");
                                    String pctChange = primaryData.getString("percentageChange");

                                    //Pass empty stock entries in this moment
                                    ArrayList<Entry> entries = new ArrayList<>();
                                    Stock stock = new Stock(symbol, companyName, lastSalePrice, netChange, pctChange, null, null, entries);
                                    getHistoricalStockData(stock, true);

                                } else {
                                    Log.e(TAG, "onDataLoaded: Symbol Not Exits");
                                    emptyViewLyBinding.titleTw.setText(R.string.symbol_not_found);
                                }

                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        @Override
                        public void onError(Exception error) {
                            Log.e(TAG, "onError: " + error.getMessage());
                            emptyViewLyBinding.titleTw.setText(error.getMessage());
                        }
                    });
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                emptyViewLyBinding.titleTw.setText(R.string.empty_view_title);
                if (!TextUtils.isEmpty(newText)) {
                    ArrayList<Stock> newStocks = new ArrayList<>();
                    // Search stocks by symbol or description
                    for (Stock stock : stockArrayList) {
                        if (stock.getSymbol().toLowerCase().contains(newText.toLowerCase()) ||
                                stock.getName().toLowerCase().contains(newText.toLowerCase())) {
                            newStocks.add(stock);
                        }
                    }
                    stockAdapter.setStocks(newStocks);
                } else {
                    // If the search box is empty, show all stocks
                    stockAdapter.setStocks(stockArrayList);
                }
                return true;
            }
        });
        binding.dateTw.setText(String.format("Last price as of %s", DateUtils.getTodayDate()));
        //get data
        getStocks();
    }

    private void getHistoricalStockData(@NonNull Stock stock, boolean isSearch) {
        stockService.getHistoricalStockData(stock.getSymbol(), 60, new StockService.StocksServiceListener() {
            @Override
            public void onDataLoaded(JSONObject data) {
                try {
                    JSONArray trades = data.getJSONObject("tradesTable").getJSONArray("rows");
                    int index = 0;
                    for (int i = trades.length() - 1; i >= 0; i--) {
                        JSONObject trade = trades.getJSONObject(i);
                        String closeStr = trade.getString("close");
                        double close = Float.parseFloat(closeStr.replace("$", "").replace(",", ""));
                        stock.getStockEntries().add(new Entry(index, (float) close));
                        index++;
                    }

                    if (isSearch) {
                        binding.recyclerView.clearOnScrollListeners();
                    } else {
                        binding.recyclerView.addOnScrollListener(onScrollListener);
                    }

                    stockAdapter.addData(stock);
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

    private void getStocks() {
        stockService = new StockService(this);
        stockService.getStocks(dataLimit, dataOffset, new StockService.StocksServiceListener() {
            @Override
            public void onDataLoaded(JSONObject data) {
                try {
                    String asOfDate = data.getString("asof");
                    //binding.dateTw.setText(asOfDate);
                    JSONArray stocks = data.getJSONObject("table").getJSONArray("rows");

                    for (int i = 0; i < stocks.length(); i++) {
                        JSONObject obj = stocks.getJSONObject(i);
                        String symbol = obj.getString("symbol");
                        String name = obj.getString("name");
                        String last_sale = obj.getString("lastsale");
                        String net_change = obj.getString("netchange");
                        String pct_change = obj.getString("pctchange");
                        String market_cap = obj.getString("marketCap");
                        String url = obj.getString("url");

                        //Pass empty list stockEntries for now
                        List<Entry> stockEntries = new ArrayList<>();
                        Stock stock = new Stock(symbol, name, last_sale, net_change, pct_change, market_cap, url, stockEntries);
                        getHistoricalStockData(stock, false);
                    }

                    //Disable splash screen when loop is finished
                    disableSplash = true;
                    isDataLoading = false;
                    binding.loadDataPb.setVisibility(View.GONE);

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
}