package com.app.checkstocks.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.github.mikephil.charting.data.Entry;

import java.util.List;

public class Stock implements Parcelable {

    String symbol, name, lastsale, netchange, pctchange, marketCap, url;
    List<Entry> stockEntries; //chart list

    public Stock() {
    }


    public Stock(String symbol, String name, String lastsale, String netchange, String pctchange, String marketCap, String url, List<Entry> stockEntries) {
        this.symbol = symbol;
        this.name = name;
        this.lastsale = lastsale;
        this.netchange = netchange;
        this.pctchange = pctchange;
        this.marketCap = marketCap;
        this.url = url;
        this.stockEntries = stockEntries;
    }


    protected Stock(Parcel in) {
        symbol = in.readString();
        name = in.readString();
        lastsale = in.readString();
        netchange = in.readString();
        pctchange = in.readString();
        marketCap = in.readString();
        url = in.readString();
        stockEntries = in.createTypedArrayList(Entry.CREATOR);
    }

    public static final Creator<Stock> CREATOR = new Creator<Stock>() {
        @Override
        public Stock createFromParcel(Parcel in) {
            return new Stock(in);
        }

        @Override
        public Stock[] newArray(int size) {
            return new Stock[size];
        }
    };

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

    public String getLastsale() {
        return lastsale;
    }

    public void setLastsale(String lastsale) {
        this.lastsale = lastsale;
    }

    public String getNetchange() {
        return netchange;
    }

    public void setNetchange(String netchange) {
        this.netchange = netchange;
    }

    public String getPctchange() {
        return pctchange;
    }

    public void setPctchange(String pctchange) {
        this.pctchange = pctchange;
    }

    public String getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(String marketCap) {
        this.marketCap = marketCap;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Entry> getStockEntries() {
        return stockEntries;
    }

    public void setStockEntries(List<Entry> stockEntries) {
        this.stockEntries = stockEntries;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(symbol);
        parcel.writeString(name);
        parcel.writeString(lastsale);
        parcel.writeString(netchange);
        parcel.writeString(pctchange);
        parcel.writeString(marketCap);
        parcel.writeString(url);
        parcel.writeTypedList(stockEntries);
    }
}
