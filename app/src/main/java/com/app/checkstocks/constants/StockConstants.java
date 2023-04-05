package com.app.checkstocks.constants;

public class StockConstants {
    public static final String BASE_API_URL = "https://api.nasdaq.com/api/";
    public static final String GET_STOCKS_URL = BASE_API_URL + "screener/stocks?tableonly=true&exchange=NASDAQ";
    public static final String GET_MARKET_MOVERS_URL = BASE_API_URL + "marketmovers?assetclass=stocks&exchangetype=nasdaq";
    public static final String GET_HISTORICAL_STOCK_URL = BASE_API_URL + "quote/%s/historical?assetclass=stocks&fromdate=%s&todate=%s&limit=%s";
    public static final String GET_STOCK_SYMBOL_SEARCH_URL = BASE_API_URL + "quote/%s/info?assetclass=stocks";

}
