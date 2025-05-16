package com.myfirstproject.example.controller;

import com.myfirstproject.example.service.MarketDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MarketDataController {

    private MarketDataService marketDataService;

    @Autowired
    public MarketDataController(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @PostMapping("/subscribe")
    public String subscribeToMarketData(@RequestParam String exchange,
                                        @RequestParam String symbol) {
        marketDataService.subscribeMarketData(symbol, exchange);
        return "Subscribed to"  +  symbol;
    }

    @PostMapping("/subscribe-depth")
    public String subscribeToDepthData(@RequestParam String exchange,
                                       @RequestParam String[] symbols) {
        marketDataService.subscribeDepth(symbols, exchange);
        return "Subscribed to depth data for " + symbols.length + " symbols";
    }
}
