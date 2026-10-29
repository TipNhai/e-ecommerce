package com.example.demo.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealtimeNotificationRedisSubscriber implements MessageListener {

    // Thư viện Spring cung cấp cái này để bơm dữ liệu vào đường ống WebSocket
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Hàm này tự động chạy khi có ai đó hét lên loa phường Redis Pub/Sub:
     * redisTemplate.convertAndSend("realtime_notifications", userId);
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        // Lấy Nội dung tin nhắn (Chính là userId mà OrderStreamConsumer vừa đẩy vào)
        String userId = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("Redis Pub/Sub vừa nhận được yêu cầu Push Notification cho User ID: {}", userId);

        // Đường ống riêng của User này. Frontend phải subscribe vào đường ống này để hứng!
        String socketDestination = "/topic/user/" + userId + "/notifications";
        
        // Bơm tin nhắn vào đường ống đó.
        // Frontend sẽ lập tức nhận được đoạn text "NEW_NOTIFICATION"
        messagingTemplate.convertAndSend(socketDestination, "NEW_NOTIFICATION");
    }
}
