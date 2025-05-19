package com.myfirstproject.example.service;

import com.myfirstproject.example.dto.CandleMarketData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CandleStickAggregator {

    private static final Logger logger = LoggerFactory.getLogger(CandleStickAggregator.class);

    // Map to store 1-minute candles: key=token_timestamp, value=CandleStick
    private final Map<String, CandleStick> candleSticks = new ConcurrentHashMap<>();

    // Map to store the latest candle timestamp for each token
    private final Map<String, Long> latestCandleTimestamps = new ConcurrentHashMap<>();

    public void processTick(CandleMarketData tick) {
        try {
            String token = tick.getToken();
            long tickTimestamp = tick.getTimestamp();

            // Round down to the nearest minute (floor)
            long candleTimestamp = Instant.ofEpochMilli(tickTimestamp)
                    .truncatedTo(ChronoUnit.MINUTES)
                    .toEpochMilli();

            String candleKey = token + "_" + candleTimestamp;

            // Get or create a candlestick for this minute
            CandleStick candle = candleSticks.computeIfAbsent(candleKey, k -> {
                CandleStick newCandle = new CandleStick();
                newCandle.setToken(token);
                newCandle.setTimestamp(candleTimestamp);
                newCandle.setOpen(tick.getLastPrice());
                newCandle.setHigh(tick.getLastPrice());
                newCandle.setLow(tick.getLastPrice());
                newCandle.setClose(tick.getLastPrice());
                newCandle.setVolume(tick.getLastTradeQuantity());
                return newCandle;
            });

            // Update the candlestick with the new tick
            updateCandleStick(candle, tick);

            // Update the latest candle timestamp for this token
            latestCandleTimestamps.put(token, Math.max(candleTimestamp,
                    latestCandleTimestamps.getOrDefault(token, 0L)));

            // Optionally, clean up old candles
            cleanupOldCandles();

        } catch (Exception e) {
            logger.error("Error processing tick for candlestick aggregation", e);
        }
    }

    private void updateCandleStick(CandleStick candle, CandleMarketData tick) {
        // Update high and low
        candle.setHigh(Math.max(candle.getHigh(), tick.getLastPrice()));
        candle.setLow(Math.min(candle.getLow(), tick.getLastPrice()));

        // Update close price
        candle.setClose(tick.getLastPrice());

        // Update volume
        candle.setVolume(candle.getVolume() + tick.getLastTradeQuantity());
    }

    private void cleanupOldCandles() {
        // Keep only the last 60 minutes of data (or adjust as needed)
        long cutoffTime = Instant.now().minus(60, ChronoUnit.MINUTES).toEpochMilli();

        List<String> keysToRemove = new ArrayList<>();
        for (Map.Entry<String, CandleStick> entry : candleSticks.entrySet()) {
            if (entry.getValue().getTimestamp() < cutoffTime) {
                keysToRemove.add(entry.getKey());
            }
        }

        for (String key : keysToRemove) {
            candleSticks.remove(key);
        }
    }

    public List<CandleStick> getCandles(String token, long fromTimestamp, long toTimestamp) {
        List<CandleStick> result = new ArrayList<>();

        for (CandleStick candle : candleSticks.values()) {
            if (candle.getToken().equals(token) &&
                    candle.getTimestamp() >= fromTimestamp &&
                    candle.getTimestamp() <= toTimestamp) {
                result.add(candle);
            }
        }

        return result;
    }

    public Map<String, List<CandleStick>> getAllCandles() {
        Map<String, List<CandleStick>> result = new HashMap<>();

        for (CandleStick candle : candleSticks.values()) {
            String token = candle.getToken();
            result.computeIfAbsent(token, k -> new ArrayList<>()).add(candle);
        }

        return result;
    }

    public CandleStick getLatestCandle(String token) {
        Long latestTimestamp = latestCandleTimestamps.get(token);
        if (latestTimestamp == null) {
            return null;
        }

        return candleSticks.get(token + "_" + latestTimestamp);
    }

    // POJO class to represent a candlestick
    public static class CandleStick {
        private String token;
        private long timestamp;
        private double open;
        private double high;
        private double low;
        private double close;
        private long volume;

        // Getters and setters
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

        public double getOpen() { return open; }
        public void setOpen(double open) { this.open = open; }

        public double getHigh() { return high; }
        public void setHigh(double high) { this.high = high; }

        public double getLow() { return low; }
        public void setLow(double low) { this.low = low; }

        public double getClose() { return close; }
        public void setClose(double close) { this.close = close; }

        public long getVolume() { return volume; }
        public void setVolume(long volume) { this.volume = volume; }

        @Override
        public String toString() {
            return "CandleStick{" +
                    "token='" + token + '\'' +
                    ", timestamp=" + timestamp +
                    ", open=" + open +
                    ", high=" + high +
                    ", low=" + low +
                    ", close=" + close +
                    ", volume=" + volume +
                    '}';
        }
    }
}
