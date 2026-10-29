package com.example.demo.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DlqRetryService {

    ConsumerFactory<String, Object> consumerFactory;
    KafkaTemplate<String, Object> kafkaTemplate;

    private static final String ORDER_DLQ_TOPIC = "order_dlq_topic";
    private static final String ORDER_MAIN_TOPIC = "order_events_topic";
    private static final String USER_DLQ_TOPIC = "user_dlq_topic";
    private static final String USER_MAIN_TOPIC = "user_registration_topic";

    /**
     * Đọc các tin nhắn đang bị kẹt trong DLQ Đơn hàng (order_dlq_topic)
     * và bắn ngược lại vào topic chính (order_events_topic) để Consumer xử lý lại.
     */
    public String retryFailedEmails() {
        return retryTopic(ORDER_DLQ_TOPIC, ORDER_MAIN_TOPIC, "dlq-order-admin-group");
    }

    /**
     * Đọc các tin nhắn đang bị kẹt trong DLQ Đăng ký User (user_dlq_topic)
     * và bắn ngược lại vào topic chính (user_registration_topic) để gửi lại Welcome Email.
     */
    public String retryFailedUserRegistrationEmails() {
        return retryTopic(USER_DLQ_TOPIC, USER_MAIN_TOPIC, "dlq-user-admin-group");
    }

    /**
     * Giải cứu toàn bộ tin nhắn từ cả Order DLQ và User DLQ
     */
    public String retryAllFailedMessages() {
        String orderResult = retryTopic(ORDER_DLQ_TOPIC, ORDER_MAIN_TOPIC, "dlq-order-admin-group");
        String userResult = retryTopic(USER_DLQ_TOPIC, USER_MAIN_TOPIC, "dlq-user-admin-group");
        return "Kết quả xử lý DLQ: [Đơn hàng: " + orderResult + "] - [User Đăng ký: " + userResult + "]";
    }

    private String retryTopic(String dlqTopic, String mainTopic, String groupId) {
        String clientId = "dlq-client-" + UUID.randomUUID();
        log.info("Bắt đầu tiến trình giải cứu tin nhắn từ DLQ [{}] về [{}]...", dlqTopic, mainTopic);

        // Tạo Consumer thủ công từ ConsumerFactory
        try (Consumer<String, Object> consumer = consumerFactory.createConsumer(groupId, clientId)) {
            // 1. Lấy tất cả partition của topic DLQ
            List<PartitionInfo> partitionInfos = consumer.partitionsFor(dlqTopic);
            if (partitionInfos == null || partitionInfos.isEmpty()) {
                log.info("Topic DLQ [{}] chưa tồn tại hoặc không có partition nào.", dlqTopic);
                return "Topic DLQ [" + dlqTopic + "] trống.";
            }
            List<TopicPartition> partitions = partitionInfos.stream()
                    .map(p -> new TopicPartition(p.topic(), p.partition()))
                    .toList();
            // 2. Gán trực tiếp partition (KHÔNG CẦN CHỜ REBALANCE, chạy ngay lập tức!)
            consumer.assign(partitions);
            // 3. Ép con trỏ quay về vị trí đầu tiên (offset 0) để đọc sạch từ tin cũ nhất
            consumer.seekToBeginning(partitions);
            // Rút dữ liệu (Chờ tối đa 3 giây)
            ConsumerRecords<String, Object> records = consumer.poll(Duration.ofSeconds(3));

            if (records.isEmpty()) {
                log.info("Không tìm thấy tin nhắn nào cần giải cứu trong DLQ [{}].", dlqTopic);
                return "Không có tin nhắn nào trong DLQ [" + dlqTopic + "] cần xử lý lại.";
            }

            int count = 0;
            // Duyệt qua từng tin nhắn và republish về topic chính
            for (ConsumerRecord<String, Object> record : records) {
                kafkaTemplate.send(mainTopic, record.key(), record.value());
                count++;
            }

            // Xác nhận (Commit Sync) đã xử lý để dịch offset, tránh đọc lại lần sau
            consumer.commitSync();

            String successMsg = String.format("Đã giải cứu thành công %d tin nhắn từ DLQ [%s] quay lại [%s]!", count, dlqTopic, mainTopic);
            log.info(successMsg);
            return successMsg;

        } catch (Exception e) {
            log.error("Lỗi khi đọc và retry tin nhắn từ DLQ [{}]: {}", dlqTopic, e.getMessage(), e);
            throw new RuntimeException("Lỗi hệ thống khi retry tin nhắn từ DLQ [" + dlqTopic + "]: " + e.getMessage(), e);
        }
    }
}
