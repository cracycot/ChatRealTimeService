package org.example.chatrealtimeservice.utils;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig  implements WebSocketMessageBrokerConfigurer {

    /**
     * Настраиваем брокер сообщений (enableSimpleBroker) и префикс для входящих сообщений (setApplicationDestinationPrefixes).
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Регистрируем конечную точку (endpoint), по которой клиенты подключаются к WebSocket.
     * Здесь /ws-chat — URL для Handshake; withSockJS() — fallback на SockJS, если WS недоступен.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat")
                .setAllowedOrigins("http://localhost:63342")
                .withSockJS();
    }
}
