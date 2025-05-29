package com.myfirstproject.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Configuration
public class SSLContextConfiguration {

    @Bean
    public SSLContext sslContext() throws Exception {
        // Option 1: Trust all certificates (not recommended for production)
        return createTrustAllSSLContext();

        // Option 2: Use custom truststore (recommended for production)
        // return createCustomTrustStoreSSLContext();
    }

    /**
     * Creates an SSLContext that trusts all certificates.
     * Warning: This should only be used for development/testing.
     */
    private SSLContext createTrustAllSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new SecureRandom());

        // Optional: Set as default SSLContext
        SSLContext.setDefault(sslContext);

        // Optional: Disable hostname verification
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

        return sslContext;
    }

    /**
     * Creates an SSLContext with a custom truststore.
     * This is the recommended approach for production.
     */
    private SSLContext createCustomTrustStoreSSLContext() throws KeyStoreException, NoSuchAlgorithmException,
            CertificateException, IOException, KeyManagementException {

        String trustStorePath = "certs/custom-truststore.jks";
        String trustStorePassword = "changeit"; // Replace with your truststore password

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (InputStream trustStoreStream = new ClassPathResource(trustStorePath).getInputStream()) {
            trustStore.load(trustStoreStream, trustStorePassword.toCharArray());
        }

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

        return sslContext;
    }

    @Bean
    public X509TrustManager trustManager() {
        return new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() { return null; }
            public void checkClientTrusted(X509Certificate[] certs, String authType) { }
            public void checkServerTrusted(X509Certificate[] certs, String authType) { }
        };
    }
}
