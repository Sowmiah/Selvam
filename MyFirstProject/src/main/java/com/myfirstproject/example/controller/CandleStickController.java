package com.myfirstproject.example.controller;

import com.myfirstproject.example.dto.CandleMarketData;
import com.myfirstproject.example.service.CandleMarketDataService;
import com.myfirstproject.example.service.CandleStickAggregator;
import com.myfirstproject.example.service.CandleStickAggregator.CandleStick;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/candles")
public class CandleStickController {

    private static final Logger logger = LoggerFactory.getLogger(CandleStickController.class);

    private final CandleStickAggregator candleStickAggregator;
    private final CandleMarketDataService marketDataService;

    @Autowired
    public CandleStickController(CandleStickAggregator candleStickAggregator, CandleMarketDataService marketDataService) {
        this.candleStickAggregator = candleStickAggregator;
        this.marketDataService = marketDataService;

        // Register as listener to process market data into candles
        marketDataService.addMarketDataListener(this::processMarketData);
    }

    private void processMarketData(CandleMarketData marketData) {
        candleStickAggregator.processTick(marketData);
    }

    @GetMapping("/{token}")
    public ResponseEntity<List<CandleStickAggregator.CandleStick>> getCandleSticks(
            @PathVariable String token,
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to) {

        try {
            // Default to last hour if not specified
            long fromTime = from != null ? from :
                    Instant.now().minus(1, ChronoUnit.HOURS).toEpochMilli();
            long toTime = to != null ? to : Instant.now().toEpochMilli();

            List<CandleStickAggregator.CandleStick> candles = candleStickAggregator.getCandles(token, fromTime, toTime);
            return ResponseEntity.ok(candles);
        } catch (Exception e) {
            logger.error("Error retrieving candle sticks for token: " + token, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/latest/{token}")
    public ResponseEntity<CandleStick> getLatestCandleStick(@PathVariable String token) {
        try {
            CandleStick latestCandle = candleStickAggregator.getLatestCandle(token);
            if (latestCandle == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(latestCandle);
        } catch (Exception e) {
            logger.error("Error retrieving latest candle stick for token: " + token, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, List<CandleStick>>> getAllCandleSticks() {
        try {
            Map<String, List<CandleStick>> allCandles = candleStickAggregator.getAllCandles();
            return ResponseEntity.ok(allCandles);
        } catch (Exception e) {
            logger.error("Error retrieving all candle sticks", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}