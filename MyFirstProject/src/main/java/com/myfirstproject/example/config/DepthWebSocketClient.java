package com.myfirstproject.example.config;
// Spring Boot WebSocket Depth Subscription with REST Controller

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.websocket.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

@ClientEndpoint
@Service
public class DepthWebSocketClient {

    private static final String WS_URI = "wss://piconnect.flattrade.in/PiConnectWSTp/";
    private Session userSession = null;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        connect();
    }

    private void connect() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, new URI(WS_URI));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.userSession = session;
        System.out.println("WebSocket Connected");
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("Received Message: " + message);
        try {
            Map<String, Object> data = objectMapper.readValue(message, Map.class);
            String type = (String) data.get("t");
            if ("df".equals(type)) {
                // handle depth feed update here
                System.out.println("Depth Feed Update: " + data);
            } else if ("dk".equals(type)) {
                System.out.println("Depth Subscription Acknowledged: " + data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("WebSocket Closed: " + closeReason);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("WebSocket Error: " + throwable.getMessage());
    }

    public void subscribeDepth(String scriptList) {
        connect();
        if (userSession != null && userSession.isOpen()) {
            try {
                String depthRequest = objectMapper.writeValueAsString(Map.of(
                        "t", "d",
                        "k", scriptList
                ));
                userSession.getAsyncRemote().sendText(depthRequest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
