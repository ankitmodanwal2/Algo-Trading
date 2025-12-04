package com.myorg.trading.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // broker for server -> client
        config.enableSimpleBroker("/topic"); // for small deployments; replace with RabbitMQ/STOMP broker for scale
        config.setApplicationDestinationPrefixes("/app"); // client -> server
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 1. For Native Clients (React @stomp/stompjs) - NO SockJS
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");

        // 2. For Legacy Clients (Optional fallback)
        registry.addEndpoint("/ws-sockjs")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
