package com.myfirstproject.example.dto;

import lombok.Data;

@Data
public class CandleMarketData {

    private String token;
    private String exchange;
    private long timestamp;
    private double lastPrice;
    private double change;
    private double openPrice;
    private double highPrice;
    private double lowPrice;
    private long volume;
    private String tradingSymbol;
    private double averageTradePrice;
    private long lastTradeQuantity;
    private long totalBuyQuantity;
    private long totalSellQuantity;
    private double ohlcOpen;
    private double ohlcHigh;
    private double ohlcLow;
    private double ohlcClose;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(double lastPrice) {
        this.lastPrice = lastPrice;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public double getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(double openPrice) {
        this.openPrice = openPrice;
    }

    public double getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(double highPrice) {
        this.highPrice = highPrice;
    }

    public double getLowPrice() {
        return lowPrice;
    }

    public void setLowPrice(double lowPrice) {
        this.lowPrice = lowPrice;
    }

    public long getVolume() {
        return volume;
    }

    public void setVolume(long volume) {
        this.volume = volume;
    }

    public String getTradingSymbol() {
        return tradingSymbol;
    }

    public void setTradingSymbol(String tradingSymbol) {
        this.tradingSymbol = tradingSymbol;
    }

    public double getAverageTradePrice() {
        return averageTradePrice;
    }

    public void setAverageTradePrice(double averageTradePrice) {
        this.averageTradePrice = averageTradePrice;
    }

    public long getLastTradeQuantity() {
        return lastTradeQuantity;
    }

    public void setLastTradeQuantity(long lastTradeQuantity) {
        this.lastTradeQuantity = lastTradeQuantity;
    }

    public long getTotalBuyQuantity() {
        return totalBuyQuantity;
    }

    public void setTotalBuyQuantity(long totalBuyQuantity) {
        this.totalBuyQuantity = totalBuyQuantity;
    }

    public long getTotalSellQuantity() {
        return totalSellQuantity;
    }

    public void setTotalSellQuantity(long totalSellQuantity) {
        this.totalSellQuantity = totalSellQuantity;
    }

    public double getOhlcOpen() {
        return ohlcOpen;
    }

    public void setOhlcOpen(double ohlcOpen) {
        this.ohlcOpen = ohlcOpen;
    }

    public double getOhlcHigh() {
        return ohlcHigh;
    }

    public void setOhlcHigh(double ohlcHigh) {
        this.ohlcHigh = ohlcHigh;
    }

    public double getOhlcLow() {
        return ohlcLow;
    }

    public void setOhlcLow(double ohlcLow) {
        this.ohlcLow = ohlcLow;
    }

    public double getOhlcClose() {
        return ohlcClose;
    }

    public void setOhlcClose(double ohlcClose) {
        this.ohlcClose = ohlcClose;
    }
}
