package com.myfirstproject.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

@Configuration
public class WebSocketSSLConfig {

    @Bean
    public WebSocketClient webSocketClient() throws NoSuchAlgorithmException, KeyManagementException {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        // Install the all-trusting trust manager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        // Create SSLSocketFactory with our all-trusting manager
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        // Set the default SSLSocketFactory for all HTTPS connections
        HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);

        // Create and configure the WebSocketClient
        StandardWebSocketClient client = new StandardWebSocketClient();
        client.getUserProperties().put("org.apache.tomcat.websocket.SSL_CONTEXT", sslContext);

        return client;
    }

    @Bean
    public HostnameVerifier hostnameVerifier() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true; // Accept all hostnames
            }
        };
    }
}
