package com.example.demo.listener;

import com.example.demo.event.UserRegistrationEvent;
import com.example.demo.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserKafkaConsumer {

    private final MailService mailService;
    private final RedisTemplate<String, Object> redisTemplate;

    @KafkaListener(topics = "user_registration_topic", groupId = "ecommerce-user-group")
    public void handleUserRegistrationEvents(UserRegistrationEvent event, Acknowledgment ack) {
        log.info("Kafka đang xử lý UserRegistrationEvent cho user: {}, email: {}", event.getUserId(), event.getEmail());

        if (event.getEmail() == null || event.getEmail().trim().isEmpty()) {
            log.warn("User {} không có email hợp lệ. Bỏ qua gửi welcome email.", event.getUserId());
            ack.acknowledge();
            return;
        }

        String lockKey = "lock:user_welcome:" + event.getUserId();
        String processedKey = "processed:user_welcome:" + event.getUserId();

        // 1. Kiểm tra tính Idempotency: tránh gửi mail chào mừng trùng lặp
        if (Boolean.TRUE.equals(redisTemplate.hasKey(processedKey))) {
            log.warn("User {} đã được gửi welcome email thành công trước đó!", event.getUserId());
            ack.acknowledge();
            return;
        }

        // 2. Chiếm khoá ngắn hạn (Distributed Lock) chặn 2 consumer cùng xử lý
        Boolean isLockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", Duration.ofSeconds(30));
        if (Boolean.FALSE.equals(isLockAcquired)) {
            log.warn("Sự kiện User {} đang được luồng khác xử lý", event.getUserId());
            return;
        }

        try {
            // 3. Gửi Welcome Email qua Thymeleaf template hiện đại
            mailService.sendWelcomeEmail(event.getEmail(), event.getFullName());

            // 4. Đánh dấu đã gửi thành công vào Redis (lưu 7 ngày)
            redisTemplate.opsForValue().set(processedKey, "COMPLETED", Duration.ofDays(7));
            log.info("Đã gửi Welcome Email thành công cho User {} ({})", event.getUserId(), event.getEmail());

            // 5. Xác nhận offset với Kafka
            ack.acknowledge();

        } catch (Exception e) {
            log.error("Lỗi khi gửi Welcome Email cho user {}: {}", event.getUserId(), e.getMessage());
            // Ném exception để DefaultErrorHandler kích hoạt Retry và chuyển DLQ nếu thất bại
            throw new RuntimeException("Lỗi Consumer gửi Welcome Email", e);
        } finally {
            // Giải phóng lock ngắn hạn
            redisTemplate.delete(lockKey);
        }
    }
}
