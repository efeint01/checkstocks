package com.app.checkstocks.services;

import android.content.Context;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.app.checkstocks.constants.Constants;
import com.app.checkstocks.constants.StockConstants;
import com.app.checkstocks.utilities.DateUtils;
import com.app.checkstocks.utilities.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class StockService {

    String TAG = "StockService";
    final VolleySingleton volleySingleton;

    public StockService(Context context) {
        volleySingleton = VolleySingleton.getInstance(context);
    }

    public void getStocks(int dataLimit, int dataOffset, @NonNull StocksServiceListener listener) {
        String URL = StockConstants.GET_STOCKS_URL + "&limit=" + dataLimit + "&offset=" + dataOffset;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                URL,
                null,
                response -> {
                    try {
                        JSONObject data = response.getJSONObject("data");
                        listener.onDataLoaded(data);
                    } catch (JSONException e) {
                        listener.onError(e);
                    }
                }, listener::onError
        );
        volleySingleton.addToRequestQueue(jsonObjectRequest);
    }

    public void getHistoricalStockData(String symbol, int dayOffset, @NonNull StocksServiceListener listener) {
        String URL = String.format(StockConstants.GET_HISTORICAL_STOCK_URL,symbol, DateUtils.getFormattedDate(-dayOffset),DateUtils.getFormattedDate(0),dayOffset);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(URL, response -> {
            try {
                JSONObject data = response.getJSONObject("data");
                listener.onDataLoaded(data);
            } catch (JSONException e) {
                listener.onError(e);
            }
        }, listener::onError) {
            @Override
            public Map<String, String> getHeaders() {
                return getNasdaqHeaders();
            }
        };
        volleySingleton.addToRequestQueue(jsonObjectRequest);
    }

    public void getMarketMovers(@NonNull MarketMoversCallback callback) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(StockConstants.GET_MARKET_MOVERS_URL, response -> {
            try {
                JSONObject data = response.getJSONObject("data");
                JSONObject stocks = data.getJSONObject("STOCKS");
                JSONObject mostActiveByShareVolume = stocks.getJSONObject("MostActiveByShareVolume");
                JSONObject mostAdvanced = stocks.getJSONObject("MostAdvanced");
                JSONObject mostDeclined = stocks.getJSONObject("MostDeclined");
                JSONObject mostActiveByDollarVolume = stocks.getJSONObject("MostActiveByDollarVolume");
                JSONObject nasdaq100Movers = stocks.getJSONObject("Nasdaq100Movers");

                callback.onSuccess(mostActiveByShareVolume, mostAdvanced, mostDeclined, mostActiveByDollarVolume, nasdaq100Movers);

            } catch (JSONException e) {
                callback.onError(e);
            }
        }, callback::onError) {
            @Override
            public Map<String, String> getHeaders() {
                return getNasdaqHeaders();
            }
        };

        volleySingleton.addToRequestQueue(jsonObjectRequest);
    }

    public void getSearchStockBySymbol(String symbol, @NonNull StocksServiceListener callback) {
        String URL = String.format(StockConstants.GET_STOCK_SYMBOL_SEARCH_URL,symbol);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(URL, callback::onDataLoaded, callback::onError) {
            @Override
            public Map<String, String> getHeaders() {
                return getNasdaqHeaders();
            }
        };
        volleySingleton.addToRequestQueue(jsonObjectRequest);
    }

    public interface MarketMoversCallback {
        void onSuccess(JSONObject mostActiveByShareVolume, JSONObject mostAdvanced, JSONObject mostDeclined,
                       JSONObject mostActiveByDollarVolume, JSONObject nasdaq100Movers);

        void onError(Exception e);
    }

    public interface StocksServiceListener {
        void onDataLoaded(JSONObject data);

        void onError(Exception error);
    }

    public Map<String, String> getNasdaqHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", Constants.NASDAQ_API_KEY);
        return headers;
    }

}
