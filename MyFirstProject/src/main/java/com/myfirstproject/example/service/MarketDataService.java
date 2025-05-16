package com.myfirstproject.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myfirstproject.example.dao.DailyLoginTokenDAO;
import com.myfirstproject.example.repository.LoginTokenRepo;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class MarketDataService extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(MarketDataService.class);
    private static final String WEBSOCKET_ENDPOINT = "wss://piconnect.flattrade.in/PiConnectWSTp/";
    //private static final String WEBSOCKET_ENDPOINT = "wss://api.flattrade.in/NorenWSTP/";

    private WebSocketSession session;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private CountDownLatch connectionLatch = new CountDownLatch(1);

    @Value("${flattrade.api.key}")
    private String apiKey;

    @Value("${flattrade.user.id}")
    private String userId;

    //@Value("${flattrade.token}")
    private String token;

    private LoginTokenRepo loginRepo;

    @Autowired
    public MarketDataService(LoginTokenRepo loginRepo) {
        this.loginRepo = loginRepo;
    }

    @PostConstruct
    public void connect() {
        try {
            StandardWebSocketClient client = new StandardWebSocketClient();

            // Use the non-deprecated approach to establish WebSocket connection
            CompletableFuture<WebSocketSession> futureSession = client.execute(
                    this,
                    WEBSOCKET_ENDPOINT
            );

            // Get the session from the completed future
            session = futureSession.get(5, TimeUnit.SECONDS);

            boolean connected = connectionLatch.await(5, TimeUnit.SECONDS);
            if (connected) {
                authenticate();
            } else {
                logger.error("Failed to connect to FlatTrade WebSocket server");
            }
        } catch (Exception e) {
            logger.error("Error connecting to WebSocket", e);
        }
    }

    @PreDestroy
    public void disconnect() {
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (IOException e) {
                logger.error("Error closing WebSocket connection", e);
            }
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        logger.info("Connected to FlatTrade WebSocket server");
        this.session = session;
        connectionLatch.countDown();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            logger.info("Received: {}", payload);

            // Parse the message and process accordingly
            Map<String, Object> response = objectMapper.readValue(payload, Map.class);

            if ("ck".equals(response.get("t"))) {
                // Connection acknowledgment
                logger.info("Connection acknowledged");
            } else if ("tk".equals(response.get("t"))) {
                // Market data update
                processMarketData(response);
            }

        } catch (Exception e) {
            logger.error("Error handling message", e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        logger.error("Transport error", exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        logger.info("Connection closed: {}", status);
        // Implement reconnection logic here if needed
    }

    private void authenticate() {
        try {

            DailyLoginTokenDAO dao = new DailyLoginTokenDAO();

            dao = loginRepo.findAll().get(0);

            Map<String, String> authRequest = new HashMap<>();
            authRequest.put("t", "c");            // Type: connect/login
            authRequest.put("uid", userId);       // User ID
            authRequest.put("actid", userId);     // Account ID (same as User ID)
            authRequest.put("susertoken", dao.getLoginToken()); // Token from the authentication process
            authRequest.put("source", "API");     // Source

            String authMessage = objectMapper.writeValueAsString(authRequest);
            session.sendMessage(new TextMessage(authMessage));
            logger.info("Authentication request sent");
        } catch (Exception e) {
            logger.error("Error during authentication", e);
        }
    }

    public void subscribeMarketData(String symbol, String exchange) {
        try {
            //symbols[0] = "BANKNIFTY";
            Map<String, Object> subscribeRequest = new HashMap<>();
            subscribeRequest.put("t", "t");       // Type: touchline subscription
            subscribeRequest.put("k", symbol); // Symbols to subscribe

            String subscribeMessage = objectMapper.writeValueAsString(subscribeRequest);
            session.sendMessage(new TextMessage(subscribeMessage));
            logger.info("Subscribed to {}: {}", exchange, String.join(", ", symbol));
        } catch (Exception e) {
            logger.error("Error subscribing to market data", e);
        }
    }

    public void subscribeDepth(String[] symbols, String exchange) {
        try {
            Map<String, Object> depthRequest = new HashMap<>();
            depthRequest.put("t", "d");           // Type: depth subscription
            depthRequest.put("k", String.join("#", symbols)); // Symbols to subscribe

            String depthMessage = objectMapper.writeValueAsString(depthRequest);
            session.sendMessage(new TextMessage(depthMessage));
            logger.info("Subscribed to depth data for {}: {}", exchange, String.join(", ", symbols));
        } catch (Exception e) {
            logger.error("Error subscribing to depth data", e);
        }
    }

    private void processMarketData(Map<String, Object> data) {
        // Extract and process the market data
        String symbol = (String) data.get("tk");
        String exchange = (String) data.get("e");

        // Process price data
        if (data.containsKey("lp")) {
            double lastPrice = Double.parseDouble((String) data.get("lp"));
            logger.info("{} | {}: Last Price = {}", exchange, symbol, lastPrice);

            // Here you would typically:
            // 1. Update your data store
            // 2. Notify any listeners/components that need the data
            // 3. Trigger any business logic based on price movements
        }

        // Implement additional processing as needed
    }
}