---
title: "Kinh nghiệm: Phân tích kiến thức Redis/Kafka và lộ trình nâng cao"
description: "Đánh giá mức độ áp dụng Redis/Kafka trong dự án hiện tại và đề xuất lộ trình nâng cấp kiến trúc lên Microservices, DLQ, Distributed Lock."
date: 2026-10-06
---

# Tóm tắt kinh nghiệm

Trong quá trình xây dựng hệ thống E-commerce hiện tại, kiến trúc đã áp dụng thành công mô hình **Event-Driven cơ bản**:
- **Redis:** Caching (`opsForValue`), Leaderboard (`opsForZSet`), List (`opsForList`), và Pub/Sub.
- **Kafka:** Decoupling việc tạo Đơn hàng / Ghi Log Audit (Sử dụng `KafkaTemplate` và `@KafkaListener` cơ bản).

Tuy nhiên, đây vẫn là mức Application Developer. Để tiến lên Senior/Microservices, dự án cần giải quyết các bài toán sau (đã được ghi chú thành lộ trình thực hành tại `demo/LEARNING_ROADMAP_KAFKA_REDIS.md`):

1. **Kafka Advanced:**
   - Xử lý lỗi với Dead Letter Queue (DLQ).
   - Quản lý Offset (Manual Acknowledgment) để tránh mất mát dữ liệu khi server crash.
   - Đảm bảo Ordering thông qua Message Keys (hiện tại đã truyền `orderId` nhưng cần kiểm chứng luồng).

2. **Redis Advanced:**
   - Xử lý concurrency (click đúp) thông qua Distributed Lock (`setIfAbsent`).
   - Cấu hình Persistence (RDB/AOF) để tránh mất dữ liệu Leaderboard.

3. **Microservices Extraction:**
   - Tách hoàn toàn `NotificationService`, `MailService` và `Kafka Consumer` sang một dự án Spring Boot chạy độc lập.

4. **Làm chủ Config & Listener (Deconstruction Learning):**
   - Không học thuộc lòng. Áp dụng phương pháp phá hoại code: Tắt `@EnableWebSocketMessageBroker` hoặc xóa `RedisMessageListenerContainer` để thấy lỗi và hiểu vai trò của từng Component.
   - Tư duy 4 mảnh ghép Config: ConnectionFactory (Bơm), Template (Vòi xịt Bắn), Container (Phễu hứng), Serializer (Máy xay JSON).
   - Tự tái tạo tính năng Global Announcement (WebSocket + Redis Pub/Sub) từ con số 0.

**Quy tắc rút ra:** Luôn phân tích điểm mù rủi ro (Crash, Concurrency, Network Failure) khi áp dụng công cụ Message Broker và In-Memory DB. Đừng coi Config là Hộp Đen.
