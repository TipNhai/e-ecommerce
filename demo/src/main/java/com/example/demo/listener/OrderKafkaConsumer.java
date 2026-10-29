package com.example.demo.listener;

import com.example.demo.entity.Order;
import com.example.demo.entity.User;
import com.example.demo.enums.NotificationType;
import com.example.demo.event.OrderEvent;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.MailService;
import com.example.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderKafkaConsumer {

    private final MailService mailService;
    private final NotificationService notificationService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional(readOnly = true)
    @KafkaListener(topics = "order_events_topic", groupId = "ecommerce-notification-group")
    public void handleOrderEvents(OrderEvent event, Acknowledgment ack) {
        log.info("Kafka đang xử lý OrderEvent: {}", event.getOrderId());
        if (!"CREATED".equals(event.getAction())) {
                ack.acknowledge();
                return;
        }
        String lockKey = "lock:order_event:" + event.getOrderId();
        String processedKey = "processed:order_created:" + event.getOrderId();
        //Kiểm tra xem đơn hàng đã thành công trước đó chưa
        if (Boolean.TRUE.equals(redisTemplate.hasKey(processedKey))){
            log.warn("Đơn hàng {} đã được gửi mail thành cng trước đó!",event.getOrderId());
            ack.acknowledge();
            return;
        }
        //Chiếm khoá ngắn hạn để chặn 2 consumer cùng xử lý đồng thời
        Boolean isLockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", Duration.ofSeconds(30));
        if (Boolean.FALSE.equals(isLockAcquired)) {
            log.warn("Đơn hàng {} đang được luồng khác xử lý",event.getOrderId());
            return;
        }
        try {
                // [TỐI ƯU 1] Lấy User và Order (kèm orderItems để tránh LazyInitializationException khi render mail)
                User user = userRepository.findById(event.getUserId()).orElse(null);
                Order order = orderRepository.findByIdWithItems(Long.valueOf(event.getOrderId())).orElse(null);

                if (user == null || order == null) {
                    log.warn("Chưa tìm thấy User hoặc Order (ID: {}). Ném lỗi để Kafka Retry lại...", event.getOrderId());
                    throw new IllegalStateException("Chưa tìm thấy User hoặc Order (ID: " + event.getOrderId() + "). Cần thử lại!");
                }

                // Gửi Email Hóa Đơn HTML Hiện Đại (Shopee Style)
                if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                    mailService.sendOrderConfirmationEmail(user.getEmail(), order, user);
                }

                //Lưu vào DB bảng Notifications (Chậm)
                String title = "Đơn hàng mới tạo thành công";
                String message = "Đơn hàng " + order.getOrderCode() + " của bạn đã được tiếp nhận.";

                // Khớp chính xác tham số: userId, title, message, type, referenceId
                notificationService.createNotification(
                        user.getId(),
                        title,
                        message,
                        NotificationType.ORDER,
                        order.getId().toString()
                );
                //ĐIỂM GIAO THOA HYBRID: BẮN QUA REDIS CHO WEB SOCKET
                // Cú pháp đúng của RedisTemplate để bắn Pub/Sub
                redisTemplate.convertAndSend("realtime_notifications", user.getId());

                //Đánh dấu thành công vào ACK
                redisTemplate.opsForValue().set(processedKey, "COMPLETED",Duration.ofHours(24));
                log.info("Xử lý thành công Event cho Đơn hàng {}", event.getOrderId());
                //xác nhận voi kafka
                ack.acknowledge();

        } catch (Exception e) {
            log.error("Lỗi khi xử lý Kafka Event cho đơn hàng {}: {}", event.getOrderId(), e.getMessage());
            // Ném lỗi ra để Kafka không ACK tin nhắn này, giúp nó retry lại sau!
            throw new RuntimeException("Kafka Consumer Error", e);
        }finally{
            //Luôn phải dọn lock ngắn hạn
            redisTemplate.delete(lockKey);
        }
    }
}

