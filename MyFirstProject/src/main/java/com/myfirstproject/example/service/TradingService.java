package com.myfirstproject.example.service;

import com.myfirstproject.example.dao.DailyLoginTokenDAO;
import com.myfirstproject.example.dto.TokenRequest;
import com.myfirstproject.example.dto.TokenResponse;
import com.myfirstproject.example.repository.LoginTokenRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class TradingService {

    @Value("${flattrade.api.key}")
    private String apiKey;

    @Value("${flattrade.api.secret}")
    private String apiSecret;

    @Value("${server.port:8080}")
    private String serverPort;

    private RestTemplate restTemplate;

    private LoginTokenRepo loginRepo;

    @Autowired
    public TradingService(RestTemplate restTemplate, LoginTokenRepo loginRepo) {
        this.restTemplate = restTemplate;
        this.loginRepo = loginRepo;
    }

    public TokenResponse exchangeRequestCodeForToken(String requestCode) throws NoSuchAlgorithmException {
        String apiTokenUrl = "https://authapi.flattrade.in/trade/apitoken";
        String securityKey = generateSecurityKey(apiKey, requestCode, apiSecret);

        TokenRequest request = new TokenRequest();
        request.setApi_key(apiKey);
        request.setRequest_code(requestCode);
        request.setApi_secret(securityKey);

        System.out.println("Exchanging request code for token...");
        ResponseEntity<TokenResponse> response = restTemplate.postForEntity(apiTokenUrl, request, TokenResponse.class);
//        ResponseEntity<String> rawResponse = restTemplate.postForEntity(apiTokenUrl, request, String.class);
//        System.out.println("Raw API Response: " + rawResponse.getBody());

        if (response.getStatusCode() == HttpStatus.OK) {
            TokenResponse tokenResponse = response.getBody();
            if (tokenResponse != null && "Ok".equals(tokenResponse.getStat())) {
                System.out.println("Token received successfully for client: " + tokenResponse.getClient() + " and token is " + tokenResponse.getToken());

                DailyLoginTokenDAO dao = new DailyLoginTokenDAO();

                dao.setLoginToken(tokenResponse.getToken());

                this.loginRepo.save(dao);

                System.out.println("Successfully Inserted Login token");
                return tokenResponse;
            } else {
                throw new RuntimeException("Failed to get token: " +
                        (tokenResponse != null ? tokenResponse.getEmsg() : "Unknown error"));
            }
        } else {
            throw new RuntimeException("Failed to exchange request code for token. Status: " + response.getStatusCode());
        }
    }

    private String generateSecurityKey(String apiKey, String requestCode, String apiSecret) throws NoSuchAlgorithmException {
        String data = apiKey + requestCode + apiSecret;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }
}
