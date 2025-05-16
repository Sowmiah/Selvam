package com.myfirstproject.example.controller;

import com.myfirstproject.example.dto.OrderUpdate;
import com.myfirstproject.example.service.OrderUpdateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PostbackController {

    private static final Logger logger = LoggerFactory.getLogger(PostbackController.class);

    @Autowired
    private OrderUpdateService orderUpdateService;

    @PostMapping("/order-updates")
    public ResponseEntity<String> handleOrderUpdates(@RequestBody OrderUpdate orderUpdate) {
        logger.info("Received order update: {}", orderUpdate);

        try {
            // Process the order update
            orderUpdateService.processOrderUpdate(orderUpdate);
            return ResponseEntity.ok("Order update processed successfully");
        } catch (Exception e) {
            logger.error("Error processing order update", e);
            return ResponseEntity.internalServerError().body("Failed to process order update: " + e.getMessage());
        }
    }
}
