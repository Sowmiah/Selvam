package com.myfirstproject.example.controller;

import com.myfirstproject.example.service.FlattradeWebSocketClient;
import com.myfirstproject.example.service.MarketDataWebSocketHandler;
import com.myfirstproject.example.service.SecureFlattradeWebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.drafts.Draft_6455;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/flattrade")
public class FlattradeDirectController {

    private static final Logger log = LoggerFactory.getLogger(FlattradeDirectController.class);

    @Autowired
    private SecureFlattradeWebSocketService webSocketService;

    @Autowired
    private MarketDataWebSocketHandler webSocketHandler;

    @PostMapping("/connect-direct")
    public ResponseEntity<String> connectDirect(
            @RequestParam String userId,
            @RequestParam String accountId,
            @RequestParam String accessToken) {

        try {
            log.info("🔗 Attempting direct connection for user: {}", userId);

            webSocketService.connectWithToken(userId, accountId, accessToken)
                    .thenAccept(connected -> {
                        if (connected) {
                            log.info("✅ Direct WebSocket connection successful!");
                        } else {
                            log.error("❌ Direct WebSocket connection failed!");
                        }
                    });

            return ResponseEntity.ok("Direct connection initiated");

        } catch (Exception e) {
            log.error("Error during direct connection", e);
            return ResponseEntity.internalServerError()
                    .body("Connection failed: " + e.getMessage());
        }
    }

    @PostMapping("/subscribe/{instrumentToken}")
    public ResponseEntity<String> subscribe(@PathVariable String instrumentToken) {
        boolean success = webSocketService.subscribe(instrumentToken);
        return success ?
                ResponseEntity.ok("✅ Subscribed to instrument: " + instrumentToken) :
                ResponseEntity.badRequest().body("❌ Failed to subscribe to: " + instrumentToken);
    }

    @PostMapping("/unsubscribe/{instrumentToken}")
    public ResponseEntity<String> unsubscribe(@PathVariable String instrumentToken) {
        boolean success = webSocketService.unsubscribe(instrumentToken);
        return success ?
                ResponseEntity.ok("✅ Unsubscribed from instrument: " + instrumentToken) :
                ResponseEntity.badRequest().body("❌ Failed to unsubscribe from: " + instrumentToken);
    }

    @GetMapping("/status")
    public ResponseEntity<String> getConnectionStatus() {
        boolean connected = webSocketService.isConnected();
        return ResponseEntity.ok(connected ?
                "🟢 Connected to Flattrade WebSocket" :
                "🔴 Not connected to Flattrade WebSocket");

    }


    @GetMapping("/connect")
    public String connectToFlattrade(
            @RequestParam String userId,
            @RequestParam String accountId,
            @RequestParam String sessionToken,
            @RequestParam String symbol) {
        try {
            URI uri = new URI("wss://piconnect.flattrade.in/PiConnectWSTp/");

            FlattradeWebSocketClient client = new FlattradeWebSocketClient(uri, userId, accountId, sessionToken, symbol);

            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null); // Use default trust managers
            SSLSocketFactory factory = sslContext.getSocketFactory();

            client.setSocketFactory(factory);
            client.connect();
            return "WebSocket connection and touchline subscription initiated for user: " + userId;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error initiating connection: " + e.getMessage();
        }
    }

    @PostMapping("/connect-candle")
    public String connectToMarket(@RequestParam String token,
                                  @RequestParam String clientCode,
                                  @RequestParam String appId) {
        try {
            return webSocketHandler.connect(token,clientCode,appId);
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Failed to connect: " + e.getMessage();
        }
    }
}
