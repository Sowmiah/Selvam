package com.myfirstproject.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myfirstproject.example.config.SecureWebSocketConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;


import java.net.URI;

@Component
public class MarketDataWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(SecureWebSocketConfig.class);
    private final CandleAggregator aggregator;
    private final ObjectMapper mapper = new ObjectMapper();

    public MarketDataWebSocketHandler(CandleAggregator aggregator) {
        this.aggregator = aggregator;
    }

    //@PostConstruct
    public String connect(String token,String clientCode, String appId) throws Exception {
        //WebSocketClient client = new StandardWebSocketClient();
        StandardWebSocketClient client = new StandardWebSocketClient();
        WebSocketHandler handler = this;

        // Create and start connection manager
        WebSocketConnectionManager manager = new WebSocketConnectionManager(
                client,
                handler,
                "wss://api.flattrade.in/socket.io/?token="+token+"&client_code="+clientCode+"&app_id="+appId
        );
        manager.setAutoStartup(true);
        manager.start();

        log.debug("WebSocket connection established using WebSocketConnectionManager.");
        //connected = true;
        return "✅ WebSocket connection established using WebSocketConnectionManager.";

    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String subscriptionMsg = "[\"{\\\"t\\\":\\\"d\\\",\\\"k\\\":\\\"NSE|22\\\"}\"]";
        session.sendMessage(new TextMessage(subscriptionMsg));
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        if (payload.contains("\"t\":\"df\"")) {
            JsonNode root = mapper.readTree(payload);
            String token = root.path("tk").asText();
            double ltp = root.path("c").asDouble();
            long volume = root.path("ltq").asLong();
            long ltt = root.path("ltt").asLong(); // epoch millis

            aggregator.processTick(token, ltp, volume, ltt);
        }
    }
}

