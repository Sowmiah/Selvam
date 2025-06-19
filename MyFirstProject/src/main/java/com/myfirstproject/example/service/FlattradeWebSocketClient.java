package com.myfirstproject.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myfirstproject.example.dto.Candle;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FlattradeWebSocketClient extends WebSocketClient {

    private final String userId;
    private final String accountId;
    private final String sessionToken;
    private final String symbol;
    private final Map<String, Candle> liveCandles = new HashMap<>();
    private final Map<String, Long> candleStartTime = new HashMap<>();

    private final ObjectMapper mapper = new ObjectMapper();

    public FlattradeWebSocketClient(URI serverUri, String userId, String accountId, String sessionToken, String symbol) {
        super(serverUri);
        this.userId = userId;
        this.accountId = accountId;
        this.sessionToken = sessionToken;
        this.symbol = symbol;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("WebSocket Connection Opened");

//        ScheduledExecutorService pingScheduler = Executors.newSingleThreadScheduledExecutor();
//        pingScheduler.scheduleAtFixedRate(() -> {
//            if (this.isOpen()) {
//                try {
//                    System.out.println("🔁 Sending ping to keep connection alive");
//                    this.send("{\"t\":\"ping\"}");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }, 20, 20, TimeUnit.SECONDS);


        Map<String, String> connectRequest = new HashMap<>();
        connectRequest.put("t", "c");
        connectRequest.put("uid", userId);
        connectRequest.put("actid", accountId);
        connectRequest.put("source", "API");
        connectRequest.put("susertoken", sessionToken);

        try {
            String json = new ObjectMapper().writeValueAsString(connectRequest);
            send(json);
            System.out.println("Sent: " + json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received: " + message);

        try {
            JsonNode jsonNode = mapper.readTree(message);
            String type = jsonNode.path("t").asText();

            switch (type) {
                case "ck":
                    System.out.println("✅ Connection Acknowledged. Subscribing to touchline...");
                    subscribeToTouchline(symbol); //NSE|22#BSE|508123#NSE|NIFTY NFO|53954 NSE|26009
                    break;

                case "tk":
                    callCandle(jsonNode);
                    System.out.println("📥 Touchline Subscription Acknowledged: " + jsonNode.toPrettyString());
                    break;

                case "tf":
                    callCandle(jsonNode);
                    System.out.println("📊 Touchline Feed: ");
                    System.out.println("Token: " + jsonNode.path("tk").asText());
                    System.out.println("Exchange: " + jsonNode.path("e").asText());
                    System.out.println("LTP: " + jsonNode.path("lp").asText());
                    String time = jsonNode.path("ft").asText();
                    System.out.println("Volume: " + jsonNode.path("v").asText());
                    break;

                default:
                    System.out.println("📄 Unknown message type: " + type);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callCandle(JsonNode jsonNode) {
        String token = jsonNode.path("tk").asText();
        double price = jsonNode.path("lp").asDouble();
        long volume = jsonNode.path("v").asLong();

        long currentMinute = System.currentTimeMillis() / 60000;

        if (!liveCandles.containsKey(token) || candleStartTime.get(token) != currentMinute) {
            // Print/Store previous candle if exists
            if (liveCandles.containsKey(token)) {
                System.out.println("🕯️ Final Candle [" + token + "] - " + liveCandles.get(token));
            }

            // Start new candle
            liveCandles.put(token, new Candle(price, volume));
            candleStartTime.put(token, currentMinute);
        } else {
            // Update existing candle
            liveCandles.get(token).update(price, volume);
        }
    }

    private void subscribeToTouchline(String scripList) {
        try {
            Map<String, String> request = new HashMap<>();
            request.put("t", "t"); // 't' = touchline subscribe
            request.put("k", scripList); // e.g., NSE|22#BSE|508123#NSE|NIFTY

            String json = mapper.writeValueAsString(request);
            send(json);
            System.out.println("➡️ Sent touchline subscription: " + json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("WebSocket Closed: " + code + ", Reason: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("WebSocket Error: " + ex.getMessage());
    }
}
