package com.myfirstproject.example.controller;

import com.myfirstproject.example.dto.CandleMarketData;
import com.myfirstproject.example.service.CandleMarketDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/market")
public class CandleMarketDataController {

    private static final Logger logger = LoggerFactory.getLogger(CandleMarketDataController.class);

    private final CandleMarketDataService marketDataService;
    private final Map<String, CandleMarketData> latestMarketData = new ConcurrentHashMap<>();

    @Autowired
    public CandleMarketDataController(CandleMarketDataService marketDataService) {
        this.marketDataService = marketDataService;

        // Register as listener to receive market data updates
        marketDataService.addMarketDataListener(this::handleMarketDataUpdate);
    }

    @PostMapping("/connect")
    public ResponseEntity<String> connectToMarketData(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            if (token == null || token.isEmpty()) {
                return ResponseEntity.badRequest().body("Token is required");
            }

            marketDataService.connectToMarketData(token);
            return ResponseEntity.ok("Connected to market data successfully");
        } catch (Exception e) {
            logger.error("Failed to connect to market data", e);
            return ResponseEntity.internalServerError().body("Failed to connect: " + e.getMessage());
        }
    }

    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribeToMarketData(@RequestBody Map<String, List<String>> request) {
        try {
            List<String> tokens = request.get("tokens");
            if (tokens == null || tokens.isEmpty()) {
                return ResponseEntity.badRequest().body("Tokens are required");
            }

            marketDataService.subscribeToMarketData(tokens);
            return ResponseEntity.ok("Subscribed to market data successfully");
        } catch (Exception e) {
            logger.error("Failed to subscribe to market data", e);
            return ResponseEntity.internalServerError().body("Failed to subscribe: " + e.getMessage());
        }
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<String> unsubscribeFromMarketData(@RequestBody Map<String, List<String>> request) {
        try {
            List<String> tokens = request.get("tokens");
            if (tokens == null || tokens.isEmpty()) {
                return ResponseEntity.badRequest().body("Tokens are required");
            }

            marketDataService.unsubscribeFromMarketData(tokens);
            return ResponseEntity.ok("Unsubscribed from market data successfully");
        } catch (Exception e) {
            logger.error("Failed to unsubscribe from market data", e);
            return ResponseEntity.internalServerError().body("Failed to unsubscribe: " + e.getMessage());
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getConnectionStatus() {
        Map<String, Object> status = new ConcurrentHashMap<>();
        status.put("connected", marketDataService.isConnected());
        status.put("subscriptionCount", latestMarketData.size());

        return ResponseEntity.ok(status);
    }

    @GetMapping("/data/{token}")
    public ResponseEntity<?> getMarketData(@PathVariable String token) {
        CandleMarketData data = latestMarketData.get(token);
        if (data == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(data);
    }

    @GetMapping("/data")
    public ResponseEntity<Map<String, CandleMarketData>> getAllMarketData() {
        return ResponseEntity.ok(latestMarketData);
    }

    private void handleMarketDataUpdate(CandleMarketData marketData) {
        String token = marketData.getToken();
        if (token != null) {
            latestMarketData.put(token, marketData);
        }
    }

    // Example endpoint to quickly test connection with a sample token
    @GetMapping("/quickConnect/{token}")
    public ResponseEntity<String> quickConnect(@PathVariable String token) {
        try {
            marketDataService.connectToMarketData(token);

            // Subscribe to a sample token (e.g., NIFTY)
            marketDataService.subscribeToMarketData(Arrays.asList("NSE|26000", "NSE|26009"));

            return ResponseEntity.ok("Connected and subscribed to sample tokens");
        } catch (Exception e) {
            logger.error("Quick connect failed", e);
            return ResponseEntity.internalServerError().body("Quick connect failed: " + e.getMessage());
        }
    }
}
