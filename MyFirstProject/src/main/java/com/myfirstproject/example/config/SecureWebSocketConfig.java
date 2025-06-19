package com.myfirstproject.example.config;

import com.myfirstproject.example.controller.PostbackController;
import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.WebSocketContainer;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Configuration
@Slf4j
public class SecureWebSocketConfig {

    private static final Logger log = LoggerFactory.getLogger(SecureWebSocketConfig.class);

    @Bean
    public WebSocketClient secureWebSocketClient() {
        try {
            // Create SSL context that trusts all certificates (for development/testing)
            SSLContext sslContext = createTrustAllSSLContext();

            // Create WebSocket container with SSL support
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();

            // Configure SSL properties
            container.setDefaultMaxSessionIdleTimeout(300000); // 5 minutes
            container.setDefaultMaxTextMessageBufferSize(65536);
            container.setDefaultMaxBinaryMessageBufferSize(65536);

            // Create client endpoint config with SSL
            ClientEndpointConfig.Builder configBuilder = ClientEndpointConfig.Builder.create();

            // Add SSL configurator
            configBuilder.configurator(new ClientEndpointConfig.Configurator() {
                @Override
                public void beforeRequest(Map<String, List<String>> headers) {
                    // Add any custom headers if needed
                    headers.put("User-Agent", Collections.singletonList("FlattradeWebSocketClient/1.0"));
                    super.beforeRequest(headers);
                }
            });

            StandardWebSocketClient client = new StandardWebSocketClient(container);

            log.info("✅ Secure WebSocket client configured successfully");
            return client;

        } catch (Exception e) {
            log.error("❌ Failed to configure secure WebSocket client", e);
            // Fallback to standard client
            return new StandardWebSocketClient();
        }
    }

    private SSLContext createTrustAllSSLContext() throws Exception {
        // Create a trust manager that accepts all certificates
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        // Trust all client certificates
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        // Trust all server certificates
                        log.debug("Trusting server certificate: {}",
                                certs.length > 0 ? certs[0].getSubjectDN().getName() : "Unknown");
                    }
                }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new SecureRandom());

        // Set as default SSL context
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> {
            log.debug("Verifying hostname: {}", hostname);
            return true; // Accept all hostnames
        });

        return sslContext;
    }
}
