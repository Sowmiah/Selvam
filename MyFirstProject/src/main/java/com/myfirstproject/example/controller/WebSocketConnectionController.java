package com.myfirstproject.example.controller;

import com.myfirstproject.example.service.CandleMarketDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.myfirstproject.example.service.WebSocketConnectionService;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/api/connection")
public class WebSocketConnectionController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConnectionController.class);

    @Autowired
    private WebSocketConnectionService webSocketConnectionService;

    @Autowired
    private CandleMarketDataService marketDataService;

    @PostMapping("/direct")
    public ResponseEntity<String> connectDirectly(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            if (token == null || token.isEmpty()) {
                return ResponseEntity.badRequest().body("Token is required");
            }

            webSocketConnectionService.connectDirectlyWithoutManager(token);
            return ResponseEntity.ok("Connected directly to WebSocket successfully");
        } catch (Exception e) {
            logger.error("Failed to connect directly to WebSocket", e);
            return ResponseEntity.internalServerError().body("Failed to connect: " + e.getMessage());
        }
    }

    @PostMapping("/managed")
    public ResponseEntity<String> connectManaged(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            if (token == null || token.isEmpty()) {
                return ResponseEntity.badRequest().body("Token is required");
            }

            webSocketConnectionService.connectUsingManager(token);
            return ResponseEntity.ok("Connected to WebSocket using connection manager successfully");
        } catch (Exception e) {
            logger.error("Failed to connect using connection manager", e);
            return ResponseEntity.internalServerError().body("Failed to connect: " + e.getMessage());
        }
    }

    @PostMapping("/disconnect")
    public ResponseEntity<String> disconnect() {
        try {
            webSocketConnectionService.disconnect();
            return ResponseEntity.ok("Disconnected from WebSocket successfully");
        } catch (Exception e) {
            logger.error("Failed to disconnect from WebSocket", e);
            return ResponseEntity.internalServerError().body("Failed to disconnect: " + e.getMessage());
        }
    }

    @GetMapping("/debug-info")
    public ResponseEntity<Map<String, Object>> getDebugInfo() {
        Map<String, Object> debugInfo = Map.of(
                "isConnected", marketDataService.isConnected(),
                "securityDetails", "SSL bypassed for testing"
        );

        return ResponseEntity.ok(debugInfo);
    }
}
