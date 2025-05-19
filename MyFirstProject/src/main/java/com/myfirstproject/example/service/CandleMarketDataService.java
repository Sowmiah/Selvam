package com.myfirstproject.example.service;

import com.myfirstproject.example.dto.CandleMarketData;
import com.myfirstproject.example.websocketclient.FlattradeMDPWebSocketClient;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class CandleMarketDataService implements FlattradeMDPWebSocketClient.MarketDataListener {

    private static final Logger logger = LoggerFactory.getLogger(CandleMarketDataService.class);

    private final FlattradeMDPWebSocketClient webSocketClient;
    private final List<MarketDataListener> listeners = new CopyOnWriteArrayList<>();

    public interface MarketDataListener {
        void onMarketData(CandleMarketData marketData);
    }

    @Autowired
    public CandleMarketDataService(FlattradeMDPWebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
        this.webSocketClient.addMarketDataListener(this);
    }

    public void connectToMarketData(String token) {
        try {
            webSocketClient.connect(token);
            logger.info("Connected to Flattrade market data");
        } catch (Exception e) {
            logger.error("Failed to connect to Flattrade market data", e);
            throw new RuntimeException("Failed to connect to market data", e);
        }
    }

    public void subscribeToMarketData(List<String> exchangeTokens) {
        try {
            logger.info("Subscribing to market data for tokens: {}", exchangeTokens);
            webSocketClient.subscribe(exchangeTokens);
        } catch (Exception e) {
            logger.error("Failed to subscribe to market data", e);
            throw new RuntimeException("Failed to subscribe to market data", e);
        }
    }

    public void unsubscribeFromMarketData(List<String> exchangeTokens) {
        try {
            logger.info("Unsubscribing from market data for tokens: {}", exchangeTokens);
            webSocketClient.unsubscribe(exchangeTokens);
        } catch (Exception e) {
            logger.error("Failed to unsubscribe from market data", e);
        }
    }

    public void addMarketDataListener(MarketDataListener listener) {
        listeners.add(listener);
    }

    public void removeMarketDataListener(MarketDataListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void onMarketData(CandleMarketData marketData) {
        logger.debug("Received market data: {}", marketData);

        // Process 1-minute candle data if needed
        processOneMinuteCandle(marketData);

        // Notify all listeners
        for (MarketDataListener listener : listeners) {
            listener.onMarketData(marketData);
        }
    }

    private void processOneMinuteCandle(CandleMarketData
                                                marketData) {
        // Logic to process and store 1-minute candle data
        // This could involve aggregating tick data into 1-minute candles
        // or handling 1-minute candles directly if the API provides them

        // Example implementation (placeholder):
        if (marketData.getTimestamp() % 60 == 0) {
            logger.info("Processing 1-minute candle for token: {}", marketData.getToken());
            // Process and store the candle data
        }
    }

    public boolean isConnected() {
        return webSocketClient.isConnected();
    }

    @PreDestroy
    public void disconnect() {
        webSocketClient.disconnect();
        logger.info("Disconnected from Flattrade market data");
    }
}