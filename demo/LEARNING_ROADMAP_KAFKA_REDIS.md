# Lộ Trình Thực Hành Nâng Cấp Kỹ Năng Redis & Kafka (Từ Monolith lên Microservices)

Tài liệu này lưu trữ lại những khoảng trống kiến thức (Tier 2 & Tier 3) về hệ thống phân tán mà dự án E-commerce hiện tại chưa áp dụng, kèm theo **lộ trình thực hành tuần tự** để bạn rèn luyện.

---

## PHẦN 1: KAFKA CHUYÊN SÂU (GIẢI QUYẾT BÀI TOÁN RỦI RO)

### 1. Vấn đề 1: Lỗi kẹt Message & Mất Dữ Liệu (Dead Letter Queue - DLQ)
- **Thiếu sót:** Hiện tại nếu gửi Email thất bại (sai mật khẩu, đứt mạng), Kafka sẽ ném lỗi `RuntimeException` liên tục làm hệ thống kẹt ở 1 tin nhắn. Hoặc nếu bạn tắt chế độ Retry, tin nhắn sẽ bị vứt bỏ vĩnh viễn (mất thông báo của khách).
- **Cách thực hành:**
    1. Chủ động đổi sai mật khẩu Email trong `application.yaml` để gây ra lỗi.
    2. Cấu hình Spring Kafka để khi lỗi xảy ra 3 lần, thay vì kẹt hoặc vứt bỏ, Spring Boot sẽ tự động chuyển tin nhắn đó sang một Topic "nhà xác" có tên là `order_dlq_topic`.
    3. Viết 1 API phụ có tên `/retry-failed-emails` để thỉnh thoảng Admin bấm vào, hệ thống sẽ đọc lại các tin nhắn trong `order_dlq_topic` để gửi lại mail.

### 2. Vấn đề 2: Tính thứ tự của Event (Message Keys)
- **Thiếu sót:** Khi bạn có 2 Consumer chạy song song trên 2 Server, và có 2 sự kiện diễn ra cực nhanh trên CÙNG 1 ĐƠN HÀNG: `UPDATE_STATUS (Đã thanh toán)` và `UPDATE_STATUS (Đang giao)`. Nếu Server 2 chạy nhanh hơn Server 1, đơn hàng có thể bị lùi trạng thái về "Đã thanh toán" một cách vô lý.
- **Cách thực hành:** hoàn thành
    1. Khi dùng `KafkaTemplate.send("order_events_topic", key, event)`, hãy đảm bảo `key` truyền vào phải là `orderId` (hiện tại bạn đang truyền `event.getOrderId()`, đó là một điểm cộng).
    2. Kiểm chứng bằng cách in log ra ID của Thread đang xử lý Kafka `Thread.currentThread().getName()`. Đảm bảo rằng mọi sự kiện của cùng 1 Đơn hàng luôn rơi vào CÙNG 1 Thread/Partition.

### 3. Vấn đề 3: Offset & Thừa nhận (Ack Mode)
- **Thiếu sót:** Hiện tại Spring Boot tự động "đánh dấu đã đọc" (Auto Commit Offset). Nếu Server bị sập nguồn ĐÚNG LÚC đang chạy dở vòng lặp gửi Mail, tin nhắn đó bị đánh dấu đã đọc nhưng mail chưa bay đi -> Mất dữ liệu ngầm.
- **Cách thực hành:** hoàn thành
    1. Chuyển cấu hình `spring.kafka.listener.ack-mode=MANUAL_IMMEDIATE` trong file `.yaml`.
    2. Cập nhật hàm xử lý: Thêm tham số `Acknowledgment ack` vào hàm `@KafkaListener`.
    3. Chỉ gọi `ack.acknowledge()` ở DÒNG CUỐI CÙNG của hàm (khi mọi thứ đã an toàn lưu DB và gửi Mail xong).

### 4. Vấn đề 4: Tính bất biến (Idempotency) & Chống trùng lặp Message
- **Thiếu sót:** Dựa vào source code hiện tại, dự án có vẻ như đang thiếu sót rào chắn vững chắc cho idempotency. Nếu Consumer xử lý xong business logic (ghi DB) thành công, nhưng sập ngay trước khi trả Ack về Broker, tin nhắn sẽ bị re-deliver, gây rủi ro lưu lặp dữ liệu hoặc gửi trùng email.
- **Cách thực hành:**hoàn thành dùng redis vì lý do tốc độ , đơn giản
    1. **Cách 1 - Dùng CSDL quản lý (Unique Constraint):** Tạo thêm cột `event_id` (hoặc lưu vào bảng log `processed_kafka_events`) chứa ID định danh Kafka Message và đánh Unique Index. Khi bị lặp, lệnh INSERT báo lỗi `DuplicateKeyException` => ta bắt lỗi và nhẹ nhàng đánh dấu đã xử lý.
    2. **Cách 2 - Dùng Redis (Distributed Lock/Cache):** Trước khi lưu DB, tạo khóa `SETNX processed:order_created:{orderId}` (hoặc dùng `setIfAbsent`). Nếu trả về `false` (đã tồn tại), ta biết event này bị lặp lại và lập tức bỏ qua.

---

## PHẦN 2: REDIS CHUYÊN SÂU (GIẢI QUYẾT BÀI TOÁN CẠNH TRANH)

### 1. Vấn đề 1: Click đúp 2 lần (Distributed Lock)
- **Thiếu sót:** Khi khách hàng vào giỏ hàng và bấm liên tục nút "Thanh toán" 10 lần trong 1 giây (do máy lag). API `/orders` sẽ được gọi 10 lần, có rủi ro trừ tồn kho 10 lần và tạo ra 10 cái đơn hàng giống hệt nhau.
- **Cách thực hành:** đã hoàn thành
    1. Viết code trong hàm `createOrder()`. Trước khi làm gì, dùng Redis set 1 key: `lock:checkout:{userId}` với thời gian sống (TTL) là 3 giây.
    2. Sử dụng câu lệnh `SETNX` (Set if Not Exists) - trong Spring là `setIfAbsent()`.
    3. Nếu hàm trả về `false` (tức là đã có lock), lập tức `throw new AppException(ErrorCode.TOO_MANY_REQUEST)`.
    4. Cố tình dùng Postman bắn 10 API đồng thời (Concurrent Requests) để test xem có chặn được 9 cái còn lại không.

### 2. Vấn đề 2: Dữ liệu bốc hơi (Persistence & Memory)
- **Thiếu sót:** Redis là bộ nhớ RAM. Nếu sập nguồn, dữ liệu sẽ mất sạch nếu bạn chưa chỉnh cấu hình.
- **Cách thực hành:**
    1. Mở file `redis.conf` trên máy của bạn (hoặc config qua Docker).
    2. Thử bật chế độ `AOF (Append Only File)` để mỗi thao tác được ghi thẳng ra ổ cứng.
    3. Sau khi bật, lưu 1 số Key, tắt nóng Container Redis đi và bật lại xem Key có còn nguyên không.

---

## PHẦN 3: BƯỚC ĐỆM MICROSERVICES (THỰC HÀNH TỔNG HỢP CUỐI)

Khi bạn đã làm tốt các bài ở trên, đây sẽ là bài Tốt Nghiệp của bạn cho toàn bộ dự án này:

1. **Khởi tạo Project thứ hai:** Tạo 1 project Spring Boot hoàn toàn mới đặt tên là `ecommerce-notification-service`.
2. **"Chuyển nhà":** Cắt toàn bộ code của `MailService`, class `OrderKafkaConsumer`, và class `RealtimeNotificationRedisSubscriber` từ project E-commerce hiện tại sang Project mới.
3. **Cấu hình độc lập:** Project mới sẽ KHÔNG nối với Database E-commerce, mà chỉ kết nối với Kafka và Redis.
4. **Vận hành:** Bật song song 2 Project lên. Bấm đặt hàng ở Project 1 -> Project 1 ghi log "Đã bắn Kafka" -> Project 2 ghi log "Đã nhận Kafka và đang gửi mail".

---

## PHẦN 4: LÀM CHỦ CẤU HÌNH (CONFIG) & LISTENER (Kế Hoạch Ngày Mai)

**Vấn đề hiện tại:** Bạn hiểu luồng đi của dữ liệu nhưng chưa thể tự tay viết ra các file `RedisConfig`, `WebSocketConfig`, hoặc các class `Listener` từ đầu. Bạn đang phụ thuộc vào việc đọc code có sẵn.
**Nguyên nhân:** Bạn đang nhìn các file này như một "khối ma thuật" (Blackbox).
**Phương pháp giải quyết:** "Giải phẫu và Tái tạo" (Deconstruction & Reconstruction).

### 1. Bài tập Giải Phẫu (Học từ việc phá hoại)
Đừng cố nhớ thuộc lòng từng dòng code. Hãy xóa nó đi để xem máy tính chửi bạn như thế nào. Ngày mai, hãy thử làm các việc sau:
- **Thử thách 1 (WebSocket):** Vào `WebSocketConfig.java`, comment lại dòng chữ `@EnableWebSocketMessageBroker`. Chạy ứng dụng và dùng Postman gọi API. Hãy đọc kỹ dòng Log báo lỗi màu đỏ. (Bài học: Annotation này là công tắc điện bật cả một nhà máy Web Socket ngầm của Spring).
- **Thử thách 2 (Redis Serializer):** Vào `RedisConfig.java`, đổi `Jackson2JsonRedisSerializer` thành `StringRedisSerializer` ở hàm `redisTemplate`. Đặt hàng lại xem lỗi gì xảy ra? (Bài học: Hiểu sâu sắc tại sao phải biến Object thành chuỗi JSON trước khi nhét vào Redis).
- **Thử thách 3 (Redis Listener):** Vào `RedisConfig.java`, comment lại toàn bộ Bean `RedisMessageListenerContainer`. Chạy ứng dụng. Tạo đơn hàng. (Bài học: Đơn hàng vẫn tạo, Redis Pub/Sub vẫn nhận, nhưng WebSocket im bặt vì "cái tai nghe" đã bị tháo ra).

### 2. Tư Duy Viết Config (Cách để không phải học thuộc)
Khi viết Config, hãy tưởng tượng bạn đang lắp ráp các ống nước:
1. **ConnectionFactory:** Đây là cái máy bơm (Nối Spring Boot vào máy chủ Redis/Kafka).
2. **Template (RedisTemplate / KafkaTemplate):** Đây là cái vòi xịt (Dùng để chủ động BẮN dữ liệu đi).
3. **Container (ListenerContainer):** Đây là cái phễu hứng (Dùng để LẮNG NGHE dữ liệu trôi về).
4. **Serializer:** Đây là cái máy xay sinh tố ở giữa (Biến cục Java Object to đùng thành nước JSON lỏng để chảy lọt qua ống).

> Khi tư duy theo 4 món đồ này, bạn sẽ tự biết trong file Config cần tạo những `@Bean` nào mà không cần chép mạng.

### 3. Bài tập Tái Tạo (Thực hành viết từ đầu)
Vào ngày mai, sau khi đã "phá" xong, hãy thử **Không nhìn code cũ**, tạo một tính năng mới tinh: **"Hệ thống thông báo toàn hệ thống (Global Announcement)"**.
- **Mô tả:** Khi Admin gọi API `/api/admin/announce`, một thông báo (như "Hệ thống bảo trì lúc 12h") sẽ được bắn tới TẤT CẢ User đang mở Web.
- **Yêu cầu thực hành:**
    1. Tự tạo file `GlobalChatWebSocketConfig.java`, đăng ký một đường ống mới tên là `/topic/global`.
    2. Tự tạo file `GlobalRedisConfig.java`, khai báo một Topic trên Redis tên là `global_announcements`.
    3. Tự viết class `GlobalListener` lắng nghe topic đó, và dùng `SimpMessagingTemplate` để bắn dữ liệu vào `/topic/global`.
    
*Làm xong bài này từ con số 0, bạn sẽ hoàn toàn làm chủ được "phép thuật" của Spring Config!*

*Hoàn thành tới đây, bạn có thể tự tin đi phỏng vấn vị trí Java Backend Mid/Senior với tư duy hệ thống phân tán sắc bén!*
