package com.example.demo.config;

import com.example.demo.listener.RealtimeNotificationRedisSubscriber;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import com.example.demo.event.OrderEvent;
import com.example.demo.listener.OrderStreamConsumer;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;
import java.util.Random;

@Configuration
@EnableCaching
public class RedisConfig {
    private Random random = new Random();
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new JdkSerializationRedisSerializer());
        
        // Cấu hình riêng cho Hash (Dùng cho Giỏ hàng) để sửa lỗi "Failed to deserialize payload"
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        //Khởi tạo Random để làm Jitter tránh Cache Avalanche (nếu tất cả redis hết hạn cùng lúc)

        long baseMinutes = 60L;
        long jitterMinutes = this.random.nextInt(15) + 1L;// Tạo số ngẫu nhiên từ 1 -> 15
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(baseMinutes + jitterMinutes))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new JdkSerializationRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }

    @Bean
    public Subscription orderStreamSubscription(
            RedisConnectionFactory factory,
            OrderStreamConsumer orderStreamConsumer ){

        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, ObjectRecord<String, OrderEvent>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        .pollTimeout(Duration.ofMillis(100))
                        .targetType(OrderEvent.class) // TRUYỀN CLASS VÀO ĐÂY, HẾT LỖI!
                        .build();
        StreamMessageListenerContainer<String, ObjectRecord<String, OrderEvent>> container =
                StreamMessageListenerContainer.create(factory, options);
        Subscription subscription = container.receive(
                org.springframework.data.redis.connection.stream.StreamOffset.latest("orders_stream"),
                orderStreamConsumer
        );
        container.start();
        return subscription;
    }

    //Distributed Lock
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        // Cấu hình kết nối tới Redis (sửa lại theo thông số nếu cần)
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379");
        // .setPassword("mật_khẩu_nếu_có");
        return Redisson.create(config);
    }

    // --- CẤU HÌNH TRẠM BƠM REDIS PUB/SUB , cho phép lắng nghe RealtimeNotificationRedisSubscriber để mở websocket
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            RealtimeNotificationRedisSubscriber subscriber) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        
        // Cắm cái loa "subscriber" vào đài phát thanh mang tên "realtime_notifications"
        container.addMessageListener(
            subscriber, 
            new ChannelTopic("realtime_notifications")
        );
        return container;
    }
}