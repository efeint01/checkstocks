package com.app.checkstocks.models;

import com.github.mikephil.charting.data.Entry;

import java.util.List;

public class StockBanner {
    String symbol, name, lastSalePrice, lastSaleChange, change, deltaIndicator;
    List<Entry> stockEntries;

    public StockBanner() {
    }


    public StockBanner(String symbol, String name, String lastSalePrice, String lastSaleChange, String change, String deltaIndicator, List<Entry> stockEntries) {
        this.symbol = symbol;
        this.name = name;
        this.lastSalePrice = lastSalePrice;
        this.lastSaleChange = lastSaleChange;
        this.change = change;
        this.deltaIndicator = deltaIndicator;
        this.stockEntries = stockEntries;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastSalePrice() {
        return lastSalePrice;
    }

    public void setLastSalePrice(String lastSalePrice) {
        this.lastSalePrice = lastSalePrice;
    }

    public String getLastSaleChange() {
        return lastSaleChange;
    }

    public void setLastSaleChange(String lastSaleChange) {
        this.lastSaleChange = lastSaleChange;
    }

    public String getChange() {
        return change;
    }

    public void setChange(String change) {
        this.change = change;
    }

    public String getDeltaIndicator() {
        return deltaIndicator;
    }

    public void setDeltaIndicator(String deltaIndicator) {
        this.deltaIndicator = deltaIndicator;
    }

    public List<Entry> getStockEntries() {
        return stockEntries;
    }

    public void setStockEntries(List<Entry> stockEntries) {
        this.stockEntries = stockEntries;
    }
}
