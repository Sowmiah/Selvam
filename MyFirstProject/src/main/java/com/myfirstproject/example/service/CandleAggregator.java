package com.myfirstproject.example.service;

import com.myfirstproject.example.dto.Candle;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class CandleAggregator {

    private final Map<String, Candle> currentCandles = new HashMap<>();

    public synchronized void processTick(String token, double price, long quantity, long epochMillis) {
        LocalDateTime minuteBucket = LocalDateTime.ofInstant(
                new java.util.Date(epochMillis).toInstant(),
                ZoneId.of("Asia/Kolkata")
        ).withSecond(0).withNano(0);

        String key = token + "-" + minuteBucket;

        Candle candle = currentCandles.get(key);
        if (candle == null) {
            candle = new Candle();
            //candle.setTimestamp(minuteBucket);
            candle.setOpen(price);
            candle.setHigh(price);
            candle.setLow(price);
            candle.setClose(price);
            candle.setVolume(quantity);
            currentCandles.put(key, candle);
        } else {
            candle.setHigh(Math.max(candle.getHigh(), price));
            candle.setLow(Math.min(candle.getLow(), price));
            candle.setClose(price);
            candle.setVolume(candle.getVolume() + quantity);
        }

        System.out.println("Updated Candle: " + candle);
    }
}
