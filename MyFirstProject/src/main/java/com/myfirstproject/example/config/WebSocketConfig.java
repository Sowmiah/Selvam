package com.myfirstproject.example.config;

import com.myfirstproject.example.websocketclient.FlattradeMDPWebSocketClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;



@Configuration
@EnableScheduling
public class WebSocketConfig {

    @Bean
    public FlattradeMDPWebSocketClient flattradeMDPWebSocketClient() {
        return new FlattradeMDPWebSocketClient();
    }

    @Bean
    public WebSocketClient webSocketClient() {
        return new StandardWebSocketClient();
    }
}
