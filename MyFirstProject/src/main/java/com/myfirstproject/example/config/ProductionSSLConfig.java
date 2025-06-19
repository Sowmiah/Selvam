package com.myfirstproject.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;

@Configuration
@Profile("production")
public class ProductionSSLConfig {

    @Value("${ssl.truststore.path:}")
    private String truststorePath;

    @Value("${ssl.truststore.password:}")
    private String truststorePassword;

    @Bean
    public SSLContext productionSSLContext() throws Exception {
        if (truststorePath != null && !truststorePath.isEmpty()) {
            // Load custom truststore for production
            KeyStore trustStore = KeyStore.getInstance("JKS");
            try (FileInputStream fis = new FileInputStream(truststorePath)) {
                trustStore.load(fis, truststorePassword.toCharArray());
            }

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);

            return sslContext;
        }

        // Use default SSL context
        return SSLContext.getDefault();
    }
}
