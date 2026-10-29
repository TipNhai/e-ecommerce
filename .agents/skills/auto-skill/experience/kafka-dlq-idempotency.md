# Kinh Nghiệm Cấu Hình Kafka, DLQ và Xử lý Idempotency

## 1. Dead Letter Queue (DLQ)
- Dự án sử dụng Spring Boot tích hợp `DefaultErrorHandler` kết hợp `DeadLetterPublishingRecoverer` và `FixedBackOff` để xử lý Retry và Dead Letter Queue.
- **Bài học**: Việc cấu hình BackOff (VD: 3 lần, mỗi lần 2s) giúp hệ thống có thời gian hồi phục cho các lỗi tạm thời (Transient Errors). Nếu quá số lần retry, thông qua `DeadLetterPublishingRecoverer`, hệ thống chuyển message lỗi sang topic `<topic_name>_dlq` để không làm chặn luồng consumer chính, đồng thời admin có thể xử lý lại bằng tay.

## 2. Message Key và Hot Partition
- Khi gửi sự kiện, dự án dùng `orderId` làm Message Key (`kafkaTemplate.send("order_events_topic", event.getOrderId(), event);`).
- **Phân tích**: Việc dùng `orderId` làm khóa đảm bảo mọi sự kiện của một đơn hàng vào chung một partition (giữ được Message Ordering).
- **Cách xử lý khi có Hot Partition**:
  - Nếu không cần ordering: bỏ Key (gửi `null`) để Kafka dùng chế độ Round-Robin.
  - Nếu cần chia tải cho 1 key lớn: Dùng kỹ thuật Salting (Key = `orderId` + `_` + `random/shard_id`) để băm nhỏ dữ liệu nhưng vẫn giới hạn ở 1 tập partition nhất định (tuy nhiên sẽ làm mất ordering gốc).

## 3. Quản lý Acknowledge và Idempotency
- **Ack Mode**: Dự án không cài đặt Manual Ack thủ công qua `Acknowledgment.acknowledge()` mà đang ném `RuntimeException` khi catch exception để Spring Boot Kafka Listener tự động retry dựa vào `DefaultErrorHandler`. 
- **Vấn đề Idempotency**: Khi Consumer tạo thông báo + gửi mail xong nhưng chết trước khi Kafka commit offset, message sẽ bị re-deliver.
- **Giải pháp**: Cần triển khai các cơ chế đảm bảo Idempotency (hiện tại code dự án chưa có đủ):
  - Dùng bảng ghi log đã xử lý (`processed_events` lưu `event_id` hoặc `Kafka message id` với Unique Constraint).
  - Dùng cơ chế khóa Redis (Distributed Cache) cho các sự kiện dựa vào mã sự kiện.
  - Sử dụng các toán tử UPSERT trong Database để đảm bảo dữ liệu không bị trùng lặp.
