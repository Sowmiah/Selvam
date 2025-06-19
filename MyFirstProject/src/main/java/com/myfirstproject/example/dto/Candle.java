package com.myfirstproject.example.dto;

import java.time.LocalDateTime;

public class Candle {
    double open;
    double high;
    double low;
    double close;
    long volume;

    // Constructors, getters, setters, toString


    public Candle(double price, long vol) {
        this.open = price;
        this.high = price;
        this.low = price;
        this.close = price;
        this.volume = vol;
    }

    public void update(double price, long vol) {
        this.high = Math.max(this.high, price);
        this.low = Math.min(this.low, price);
        this.close = price;
        this.volume += vol;
    }

    @Override
    public String toString() {
        return "O: " + open + " H: " + high + " L: " + low + " C: " + close + " V: " + volume;
    }

    public Candle() {

    }


    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getOpen() {
        return open;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public double getClose() {
        return close;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public long getVolume() {
        return volume;
    }

    public void setVolume(long volume) {
        this.volume = volume;
    }
}
