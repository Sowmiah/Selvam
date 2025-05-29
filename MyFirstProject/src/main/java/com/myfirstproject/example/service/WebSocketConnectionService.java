package com.myfirstproject.example.service;

import com.myfirstproject.example.websocketclient.FlattradeMDPWebSocketClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class WebSocketConnectionService {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketConnectionService.class);

    @Value("${flattrade.mdp.url:wss://api.flattrade.in/stream}")
    private String flattradeWebSocketUrl;

    @Autowired
    private WebSocketClient webSocketClient;

    @Autowired
    private FlattradeMDPWebSocketClient flattradeMDPWebSocketClient;

    @Autowired
    private ObjectMapper objectMapper;

    private WebSocketConnectionManager connectionManager;

    /**
     * Alternative approach to connect to WebSocket using WebSocketConnectionManager
     */
    public void connectUsingManager(String token) {
        URI uri = URI.create(flattradeWebSocketUrl + token);

        // Create connection manager
        connectionManager = new WebSocketConnectionManager(
                webSocketClient,
                flattradeMDPWebSocketClient,
                uri.toString()
        );

        // Start the connection
        connectionManager.start();

        logger.info("Started WebSocket connection using WebSocketConnectionManager");
    }

    /**
     * Direct raw connection approach for testing purposes
     */
    public void connectDirectlyWithoutManager(String token) throws Exception {
        URI uri = URI.create(flattradeWebSocketUrl + token);

        final CountDownLatch connectionLatch = new CountDownLatch(1);
        final WebSocketHandler handler = new TextWebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) {
                logger.info("Direct WebSocket connection established");
                connectionLatch.countDown();
            }

            @Override
            public void handleTextMessage(WebSocketSession session, TextMessage message) {
                logger.info("Received message: {}", message.getPayload());
            }
        };

        // Execute the connection
        webSocketClient.execute(handler, null, uri);

        // Wait for connection
        if (!connectionLatch.await(100, TimeUnit.SECONDS)) {
            throw new RuntimeException("Failed to connect to WebSocket");
        }

        logger.info("Successfully connected to WebSocket directly");
    }

    /**
     * Test method to send a simple ping message to verify connection
     */
    public boolean sendPingMessage(WebSocketSession session) {
        try {
            Map<String, String> pingMessage = new HashMap<>();
            pingMessage.put("a", "ping");

            String jsonMessage = objectMapper.writeValueAsString(pingMessage);
            session.sendMessage(new TextMessage(jsonMessage));

            logger.info("Sent ping message");
            return true;
        } catch (IOException e) {
            logger.error("Failed to send ping message", e);
            return false;
        }
    }

    /**
     * Close the WebSocket client connection
     */
    public void disconnect() {
        if (connectionManager != null) {
            connectionManager.stop();
            logger.info("Stopped WebSocket connection");
        }
    }
}
