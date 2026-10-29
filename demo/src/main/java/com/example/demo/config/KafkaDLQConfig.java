package com.example.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration
public class KafkaDLQConfig {

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> template){

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template,
                (ConsumerRecord<?,?> record, Exception exception) -> {
                    // Recoverer: Xử lý bước cuối cùng khi đã hết số lần Retry.
                    log.error("Xử lý Kafka record thất bại hoàn toàn sau retry, đang đẩy vào DLQ. Topic: {}, Key: {}, Lỗi: {}",
                            record.topic(), record.key(), exception.getMessage());
                    if ("user_registration_topic".equals(record.topic())) {
                        return new TopicPartition("user_dlq_topic", -1);
                    }
                    return new TopicPartition("order_dlq_topic", -1);
                });
        // 2. BackOff: Cấu hình chiến lược thử lại (Retry)
        // FixedBackOff(thời_gian_chờ_giữa_mỗi_lần_ms, số_lần_thử_lại_tối_đa)
        // Ví dụ: Chờ 2 giây, thử lại tối đa 3 lần
        FixedBackOff backOff = new FixedBackOff(2000L, 3);
        // 3. Kết hợp lại thành ErrorHandler
        return new DefaultErrorHandler(recoverer,backOff);
    }
}
