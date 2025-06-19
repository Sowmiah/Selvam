package com.myfirstproject.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myfirstproject.example.config.SecureWebSocketConfig;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;

import javax.net.ssl.SSLException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class SecureFlattradeWebSocketService implements WebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(SecureFlattradeWebSocketService.class);

    @Autowired
    private WebSocketClient webSocketClient;

    private static final String WEBSOCKET_URL = "wss://piconnect.flattrade.in/PiConnectWSTp/";
    private static final int CONNECTION_TIMEOUT = 30000; // 30 seconds
    private static final int MAX_RETRY_ATTEMPTS = 3;

    private WebSocketSession webSocketSession;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final AtomicInteger retryCount = new AtomicInteger(0);
    private WebSocketConnectionManager connectionManager;

    // Connection parameters
    private String userId;
    private String accountId;
    private String accessToken;

    public CompletableFuture<Boolean> connectWithToken(String userId, String accountId, String accessToken) {
        this.userId = userId;
        this.accountId = accountId;
        this.accessToken = accessToken;

        return connectToWebSocketWithRetry();
    }

    private CompletableFuture<Boolean> connectToWebSocketWithRetry() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        connectToWebSocket().thenAccept(success -> {
            if (success) {
                future.complete(true);
            } else {
                // Retry connection
                int attempts = retryCount.incrementAndGet();
                if (attempts <= MAX_RETRY_ATTEMPTS) {
                    log.warn("⚠️ Connection attempt {} failed, retrying...", attempts);

                    // Exponential backoff delay
                    int delay = (int) Math.pow(2, attempts) * 1000;

                    CompletableFuture.delayedExecutor(delay, TimeUnit.MILLISECONDS)
                            .execute(() -> {
                                connectToWebSocketWithRetry().thenAccept(future::complete);
                            });
                } else {
                    log.error("❌ Max retry attempts ({}) exceeded", MAX_RETRY_ATTEMPTS);
                    future.complete(false);
                }
            }
        });

        return future;
    }

    private CompletableFuture<Boolean> connectToWebSocket() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        try {
            log.info("🔗 Connecting to Flattrade WebSocket: {}", WEBSOCKET_URL);
            log.info("🔒 SSL/TLS connection will be established...");

            // Create connection manager with timeout
            connectionManager = new WebSocketConnectionManager(
                    webSocketClient, this, WEBSOCKET_URL);

            // Set connection timeout
            connectionManager.setAutoStartup(true);

            connectionManager.start();

            // Wait for connection with timeout
            CompletableFuture.delayedExecutor(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                    .execute(() -> {
                        if (!future.isDone()) {
                            log.warn("⏰ Connection timeout after {}ms", CONNECTION_TIMEOUT);
                            future.complete(false);
                        }
                    });

        } catch (Exception e) {
            log.error("❌ Failed to initiate WebSocket connection", e);
            handleConnectionError(e);
            future.complete(false);
        }

        return future;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("✅ SSL/TLS WebSocket connection established successfully");
        log.info("🔒 Connection is secure: {}", session.getUri().getScheme().equals("wss"));

        this.webSocketSession = session;

        // Log connection details
        log.info("📡 Remote address: {}", session.getRemoteAddress());
        log.info("🆔 Session ID: {}", session.getId());

        // Send connect request immediately
        sendConnectRequest();
    }

    private void sendConnectRequest() {
        try {
            Map<String, String> connectRequest = new HashMap<>();
            connectRequest.put("t", "c");
            connectRequest.put("uid", userId);
            connectRequest.put("actid", accountId);
            connectRequest.put("source", "API");
            connectRequest.put("susertoken", accessToken);

            String jsonRequest = objectMapper.writeValueAsString(connectRequest);
            log.info("📤 Sending secure connect request");
            log.debug("📋 Connect payload: {}", jsonRequest);

            webSocketSession.sendMessage(new TextMessage(jsonRequest));

        } catch (Exception e) {
            log.error("❌ Failed to send connect request", e);
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String payload = message.getPayload().toString();
        log.debug("📨 Received secure message: {}", payload);

        try {
            JsonNode jsonNode = objectMapper.readTree(payload);
            handleJsonMessage(jsonNode);
        } catch (Exception e) {
            log.error("❌ Error processing message: {}", payload, e);
        }
    }

    private void handleJsonMessage(JsonNode message) {
        String messageType = message.has("t") ? message.get("t").asText() : "";

        switch (messageType) {
            case "ck":
                handleConnectAck(message);
                break;
            case "tk":
                handleTickData(message);
                break;
            case "tf":
                handleFullMarketData(message);
                break;
            default:
                log.debug("📋 Unknown message type '{}': {}", messageType, message);
        }
    }

    private void handleConnectAck(JsonNode message) {
        String status = message.has("s") ? message.get("s").asText() : "";
        String uid = message.has("uid") ? message.get("uid").asText() : "";

        if ("OK".equalsIgnoreCase(status)) {
            log.info("🎉 Secure connection authenticated successfully for user: {}", uid);
            isConnected.set(true);
            retryCount.set(0); // Reset retry counter
            log.info("🚀 Ready to receive real-time data securely!");
        } else {
            log.error("❌ Authentication failed - Status: {} for user: {}", status, uid);
            log.error("💡 Check your credentials: userId, accountId, and accessToken");
            isConnected.set(false);
        }
    }

    private void handleTickData(JsonNode message) {
        log.info("📊 Secure tick data: {}", message);
        // Process tick data here
    }

    private void handleFullMarketData(JsonNode message) {
        log.info("📈 Secure market data: {}", message);
        // Process full market data here
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("🔥 WebSocket transport error", exception);
        handleConnectionError(exception);
        isConnected.set(false);
    }

    private void handleConnectionError(Throwable exception) {
        if (exception instanceof SSLException) {
            log.error("🔒 SSL/TLS Error: {}", exception.getMessage());
            log.error("💡 Possible solutions:");
            log.error("   - Check if Flattrade servers are accessible");
            log.error("   - Verify your network allows WSS connections");
            log.error("   - Check firewall/proxy settings");
        } else if (exception instanceof ConnectException) {
            log.error("🌐 Connection Error: {}", exception.getMessage());
            log.error("💡 Possible solutions:");
            log.error("   - Check internet connectivity");
            log.error("   - Verify Flattrade WebSocket URL is correct");
            log.error("   - Check if service is down");
        } else if (exception instanceof SocketTimeoutException) {
            log.error("⏰ Timeout Error: {}", exception.getMessage());
            log.error("💡 Connection took too long - will retry");
        } else {
            log.error("❌ Unknown connection error: {}", exception.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("🔌 Secure WebSocket connection closed");
        log.info("📋 Close status: {} - {}", closeStatus.getCode(), closeStatus.getReason());

        isConnected.set(false);
        this.webSocketSession = null;

        // Log close reason details
        if (closeStatus.getCode() == 1006) {
            log.warn("⚠️ Abnormal closure - possible network issue");
        } else if (closeStatus.getCode() == 1000) {
            log.info("✅ Normal closure");
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public boolean subscribe(String instrumentToken) {
        if (!isConnected.get() || webSocketSession == null) {
            log.error("❌ Cannot subscribe - secure connection not established");
            return false;
        }

        try {
            Map<String, String> subscribeRequest = new HashMap<>();
            subscribeRequest.put("t", "t");
            subscribeRequest.put("k", instrumentToken);

            String jsonRequest = objectMapper.writeValueAsString(subscribeRequest);
            log.info("📝 Subscribing securely to instrument: {}", instrumentToken);

            webSocketSession.sendMessage(new TextMessage(jsonRequest));
            return true;

        } catch (Exception e) {
            log.error("❌ Failed to subscribe to instrument: {}", instrumentToken, e);
            return false;
        }
    }

    public boolean unsubscribe(String instrumentToken) {
        if (!isConnected.get() || webSocketSession == null) {
            log.error("❌ Cannot unsubscribe - secure connection not established");
            return false;
        }

        try {
            Map<String, String> unsubscribeRequest = new HashMap<>();
            unsubscribeRequest.put("t", "u");
            unsubscribeRequest.put("k", instrumentToken);

            String jsonRequest = objectMapper.writeValueAsString(unsubscribeRequest);
            log.info("🗑️ Unsubscribing securely from instrument: {}", instrumentToken);

            webSocketSession.sendMessage(new TextMessage(jsonRequest));
            return true;

        } catch (Exception e) {
            log.error("❌ Failed to unsubscribe from instrument: {}", instrumentToken, e);
            return false;
        }
    }

    public boolean isConnected() {
        return isConnected.get();
    }

    public boolean isSecureConnection() {
        return webSocketSession != null &&
                webSocketSession.getUri().getScheme().equals("wss");
    }

    @PreDestroy
    public void disconnect() {
        try {
            if (connectionManager != null) {
                connectionManager.stop();
            }
            if (webSocketSession != null && webSocketSession.isOpen()) {
                webSocketSession.close(CloseStatus.NORMAL);
            }
            log.info("🛑 Secure WebSocket service disconnected gracefully");
        } catch (Exception e) {
            log.error("❌ Error during secure disconnect", e);
        }
    }
}