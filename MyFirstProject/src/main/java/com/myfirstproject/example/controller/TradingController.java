package com.myfirstproject.example.controller;

import com.myfirstproject.example.dao.DailyLoginTokenDAO;
import com.myfirstproject.example.dao.UserDAO;
import com.myfirstproject.example.dto.DailyLoginToken;
import com.myfirstproject.example.dto.TokenRequest;
import com.myfirstproject.example.dto.TokenResponse;
import com.myfirstproject.example.dto.UserDTO;
import com.myfirstproject.example.service.MyService;
import com.myfirstproject.example.service.TradingService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

@RestController
public class TradingController {

    private MyService service;

    private TradingService tradingService;

    @Value("${flattrade.api.key}")
    private String apiKey;

    @PostConstruct
    public void init() {
        try {
            // Open the authorization URL in the default browser
            String authUrl = "https://auth.flattrade.in/?app_key=" + apiKey;
            System.out.println("Opening browser with URL: " + authUrl);
            Desktop.getDesktop().browse(new URI(authUrl));
        } catch (Exception e) {
            System.err.println("Failed to open browser: " + e.getMessage());
            System.out.println("Please manually open the following URL in your browser:");
            System.out.println("https://auth.flattrade.in/?app_key=" + apiKey);
        }
    }

    @Autowired
    public TradingController(MyService service, TradingService tradingService) {
        this.service = service;
        this.tradingService = tradingService;
    }

    @GetMapping("/saveHello")
    public ResponseEntity<String> createAllItems() {

        return new ResponseEntity<>(service.setAllItems(), HttpStatus.OK);
    }

    @GetMapping("/getHello")
    public ResponseEntity<List<UserDTO>> getAllItems() {

        System.out.println("**********************");
        //service.nseBankNiftyFetcher();
        System.out.println("**********************");
        return new ResponseEntity<>(service.getAllItems(), HttpStatus.OK);
    }

    @GetMapping("/callback")
    @ResponseBody
    public String handleCallback(@RequestParam("code") String requestCode,
    @RequestParam("client") String client) {
        try {
            TokenResponse tokenResponse = tradingService.exchangeRequestCodeForToken(requestCode);
            return "Authorization successful! Token received for client: " + tokenResponse.getClient() +
                    "<br><br>You can close this window now.";
        } catch (Exception e) {
            return "Error during token exchange: " + e.getMessage();
        }
    }

}
