package com.example.demo.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Service;
import com.example.demo.event.OrderEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderStreamConsumer implements StreamListener<String , ObjectRecord<String, OrderEvent>> {

    @Override
    public void onMessage(ObjectRecord<String, OrderEvent> message) {
        OrderEvent evenData = message.getValue();
        String orderId = evenData.getOrderId();
        String userId = evenData.getUserId();

        log.info("Bắt được đơn hàng mới: ID = {}. Đang tiến hành gửi Email ngầm...", orderId);

        try {
            // Thực hiện logic nặng nề ở đây (Chọc DB lấy Email User, kết nối tới Gmail SMTP...)
            // mailService.sendOrderConfirmation(userId, orderId);

            // Giả lập thời gian gửi mail tốn 2 giây
            Thread.sleep(2000);
            log.info("Đã gửi Email thành công cho User: {}", userId);

            // LƯU Ý KHI LÊN PRODUCTION: phải cấu hình lệnh ACK để báo Redis xóa tin nhắn này.
            // (Bước 3 sẽ hướng dẫn cấu hình container tự động ACK).

        } catch (Exception e) {
            log.error("Lỗi khi gửi email cho đơn hàng {}: {}", orderId, e.getMessage());
            // Lỗi thì tin nhắn vẫn còn đó, Redis sẽ không xóa, ta có thể chạy lại sau!
            Thread.currentThread().interrupt();
        }
    }
}
