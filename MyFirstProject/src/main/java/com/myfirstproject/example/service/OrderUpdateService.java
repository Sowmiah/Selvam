package com.myfirstproject.example.service;

import com.myfirstproject.example.dto.OrderUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderUpdateService {

    private static final Logger logger = LoggerFactory.getLogger(OrderUpdateService.class);

    public void processOrderUpdate(OrderUpdate orderUpdate) {
        // Log the order update
        logger.info("Processing order update: {}", orderUpdate);

        // Here you would implement your business logic based on the order update
        // For example:
        switch (orderUpdate.getStatus()) {
            case "COMPLETE":
                handleCompletedOrder(orderUpdate);
                break;
            case "REJECTED":
                handleRejectedOrder(orderUpdate);
                break;
            case "PENDING":
                handlePendingOrder(orderUpdate);
                break;
            case "CANCELED":
                handleCanceledOrder(orderUpdate);
                break;
            default:
                logger.warn("Unhandled order status: {}", orderUpdate.getStatus());
        }
    }

    private void handleCompletedOrder(OrderUpdate orderUpdate) {
        logger.info("Order completed: {} {} {} {} @ {}",
                orderUpdate.getNorenOrderNo(),
                orderUpdate.getTransactionType(),
                orderUpdate.getFilledShares(),
                orderUpdate.getTradingSymbol(),
                orderUpdate.getAveragePrice());

        // Add your business logic for completed orders
        // For example: update your database, notify the user, etc.
    }

    private void handleRejectedOrder(OrderUpdate orderUpdate) {
        logger.warn("Order rejected: {} - Reason: {}",
                orderUpdate.getNorenOrderNo(),
                orderUpdate.getRemarks());

        // Add your business logic for rejected orders
    }

    private void handlePendingOrder(OrderUpdate orderUpdate) {
        logger.info("Order pending: {}", orderUpdate.getNorenOrderNo());

        // Add your business logic for pending orders
    }

    private void handleCanceledOrder(OrderUpdate orderUpdate) {
        logger.info("Order canceled: {}", orderUpdate.getNorenOrderNo());

        // Add your business logic for canceled orders
    }
}
