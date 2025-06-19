package com.myfirstproject.example.config;

import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.List;

@Configuration 
public class WebSocketClientConfig {

    public WebSocketStompClient stompClient() {
        List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);
        return new WebSocketStompClient(sockJsClient);
    }
}
