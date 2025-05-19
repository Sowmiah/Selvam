package com.myfirstproject.example.websocketclient;

import com.myfirstproject.example.dto.CandleMarketData;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@Component
public class FlattradeMDPWebSocketClient extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(FlattradeMDPWebSocketClient.class);
    //private static final String FLATTRADE_MDP_WS_URL = "wss://mdp.flattrade.in/ftws/";
    private static final String FLATTRADE_MDP_WS_URL = "wss://api.flattrade.in/stream";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private WebSocketSession webSocketSession;
    private CountDownLatch connectionLatch = new CountDownLatch(1);
    private List<MarketDataListener> listeners = new ArrayList<>();

    public interface MarketDataListener {
        void onMarketData(CandleMarketData marketData);
    }

    public void connect(String token) {
        try {
            WebSocketClient client = new StandardWebSocketClient();

            // Build URL with token as query parameter
            String url = FLATTRADE_MDP_WS_URL + token;

            logger.info("Connecting to Flattrade MDP WebSocket: {}", url);
            client.execute(this, null, java.net.URI.create(url));

            // Wait for connection to be established
            if (!connectionLatch.await(100, TimeUnit.SECONDS)) {
                logger.error("Timeout waiting for WebSocket connection");
                throw new RuntimeException("Failed to connect to Flattrade WebSocket");
            }
        } catch (Exception e) {
            logger.error("Error connecting to Flattrade WebSocket", e);
            throw new RuntimeException("Failed to connect to Flattrade WebSocket", e);
        }
    }

    public void addMarketDataListener(MarketDataListener listener) {
        listeners.add(listener);
    }

    public void removeMarketDataListener(MarketDataListener listener) {
        listeners.remove(listener);
    }

    public void subscribe(List<String> exchangeTokens) {
        try {
            if (webSocketSession == null || !webSocketSession.isOpen()) {
                logger.error("WebSocket session is not established");
                return;
            }

            Map<String, Object> request = new HashMap<>();
            request.put("t", "t"); // Type: tick
            request.put("k", exchangeTokens);

            String message = objectMapper.writeValueAsString(request);
            logger.info("Subscribing to market data: {}", message);

            webSocketSession.sendMessage(new TextMessage(message));
        } catch (Exception e) {
            logger.error("Error subscribing to market data", e);
        }
    }

    public void unsubscribe(List<String> exchangeTokens) {
        try {
            if (webSocketSession == null || !webSocketSession.isOpen()) {
                logger.error("WebSocket session is not established");
                return;
            }

            Map<String, Object> request = new HashMap<>();
            request.put("t", "u"); // Type: unsubscribe
            request.put("k", exchangeTokens);

            String message = objectMapper.writeValueAsString(request);
            logger.info("Unsubscribing from market data: {}", message);

            webSocketSession.sendMessage(new TextMessage(message));
        } catch (Exception e) {
            logger.error("Error unsubscribing from market data", e);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        logger.info("WebSocket connection established to Flattrade MDP");
        this.webSocketSession = session;
        connectionLatch.countDown();
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            logger.debug("Received market data: {}", payload);

            JsonNode node = objectMapper.readTree(payload);

            // Check if it's a successful response
            if (node.has("s") && "success".equals(node.get("s").asText())) {
                logger.info("Successfully received response: {}", payload);
                return;
            }

            // Parse as MarketData
            CandleMarketData marketData = parseMarketData(node);

            // Notify listeners
            for (MarketDataListener listener : listeners) {
                listener.onMarketData(marketData);
            }
        } catch (Exception e) {
            logger.error("Error handling market data message", e);
        }
    }

    private CandleMarketData parseMarketData(JsonNode node) {
        try {
            CandleMarketData marketData = new CandleMarketData();

            if (node.has("tk")) marketData.setToken(node.get("tk").asText());
            if (node.has("e")) marketData.setExchange(node.get("e").asText());
            if (node.has("ts")) marketData.setTimestamp(node.get("ts").asLong());
            if (node.has("lp")) marketData.setLastPrice(node.get("lp").asDouble());
            if (node.has("c")) marketData.setChange(node.get("c").asDouble());
            if (node.has("o")) marketData.setOpenPrice(node.get("o").asDouble());
            if (node.has("h")) marketData.setHighPrice(node.get("h").asDouble());
            if (node.has("l")) marketData.setLowPrice(node.get("l").asDouble());
            if (node.has("v")) marketData.setVolume(node.get("v").asLong());

            // Add more fields as per Flattrade's API documentation

            return marketData;
        } catch (Exception e) {
            logger.error("Error parsing market data", e);
            throw new RuntimeException("Failed to parse market data", e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        logger.error("WebSocket transport error", exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        logger.info("WebSocket connection closed: {}", status);
        this.webSocketSession = null;

        // Reset the connection latch for the next connection attempt
        connectionLatch = new CountDownLatch(1);
    }

    public boolean isConnected() {
        return webSocketSession != null && webSocketSession.isOpen();
    }

    public void disconnect() {
        try {
            if (webSocketSession != null && webSocketSession.isOpen()) {
                webSocketSession.close();
            }
        } catch (IOException e) {
            logger.error("Error closing WebSocket connection", e);
        }
    }
}