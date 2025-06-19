package com.myfirstproject.example.controller;

import com.myfirstproject.example.config.DepthWebSocketClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/depth/subscription")
class DepthSubscriptionController {

    private final DepthWebSocketClient depthWebSocketClient;

    @Autowired
    public DepthSubscriptionController(DepthWebSocketClient depthWebSocketClient) {
        this.depthWebSocketClient = depthWebSocketClient;
    }

    @PostMapping("/subscribe-depth")
    public String subscribeToDepth(@RequestParam String scripts) {
        depthWebSocketClient.subscribeDepth(scripts);
        return "Subscribed to depth for: " + scripts;
    }
}
