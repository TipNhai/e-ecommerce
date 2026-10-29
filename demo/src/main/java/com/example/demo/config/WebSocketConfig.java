package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // Annotation này sẽ giúp tự động tạo SimpMessagingTemplate
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Đây là điểm mà Frontend (React/Vue/JS) sẽ gọi tới để mở đường ống kết nối
        // Ví dụ: ws://localhost:8080/e-commerce/ws
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Cho phép gọi từ mọi domain (CORS)
                .withSockJS(); // Fallback nếu trình duyệt cũ không hỗ trợ WebSockets
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Cấu hình tiền tố cho các đường ống gửi tin từ Server xuống Client
        registry.enableSimpleBroker("/topic");
        
        // Cấu hình tiền tố nếu Client muốn gửi tin lên Server (Chat)
        registry.setApplicationDestinationPrefixes("/app");
    }
}
