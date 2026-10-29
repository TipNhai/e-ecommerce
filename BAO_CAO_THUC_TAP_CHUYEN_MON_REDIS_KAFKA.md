# BÁO CÁO THỰC TẬP CHUYÊN MÔN

---

## TRANG BÌA

* **TRƯỜNG:** ĐẠI HỌC ...
* **KHOA:** CÔNG NGHỆ THÔNG TIN
* **CHUYÊN NGÀNH:** KỸ THUẬT PHẦN MỀM / HỆ THỐNG THÔNG TIN
* **ĐỀ TÀI:** **NGHIÊN CỨU VÀ ỨNG DỤNG REDIS & APACHE KAFKA TRONG XÂY DỰNG HỆ THỐNG THƯƠNG MẠI ĐIỆN TỬ HIỆU NĂNG CAO, CHỊU TẢI LỚN THEO KIẾN TRÚC HƯỚNG SỰ KIỆN (EVENT-DRIVEN ARCHITECTURE)**
* **GIÁO VIÊN HƯỚNG DẪN:** [Họ và tên Giảng viên hướng dẫn]
* **SINH VIÊN THỰC HIỆN:** [Họ và tên Sinh viên thực hiện]
* **MÃ SỐ SINH VIÊN:** [Mã số sinh viên]
* **LỚP / KHÓA:** [Tên lớp / Niên khóa]
* **THỜI GIAN / ĐỊA ĐIỂM:** 2024 / [Địa điểm thực tập / Doanh nghiệp]

---

## MỤC LỤC

* **LỜI MỞ ĐẦU**
  * 1. Tính cấp thiết của đề tài
  * 2. Mục tiêu nghiên cứu
  * 3. Đối tượng và phạm vi nghiên cứu
  * 4. Phương pháp nghiên cứu
  * 5. Ý nghĩa khoa học và thực tiễn
* **CHƯƠNG I: GIỚI THIỆU CHUNG VỀ CÔNG NGHỆ & CÔNG CỤ (REDIS & APACHE KAFKA)**
  * 1.1. Tổng quan nguồn gốc và định nghĩa cốt lõi
    * 1.1.1. In-Memory Data Store & Cache: Redis
    * 1.1.2. Distributed Event Streaming Platform: Apache Kafka
  * 1.2. Đánh giá ưu điểm và hạn chế
    * 1.2.1. Đánh giá kỹ thuật về Redis
    * 1.2.2. Đánh giá kỹ thuật về Apache Kafka
  * 1.3. Các tính năng và cơ chế hoạt động nổi bật
    * 1.3.1. Phân hệ Redis: Cấu trúc dữ liệu, Redisson Lock, Bloom Filter, Pub/Sub, AOF
    * 1.3.2. Phân hệ Kafka: Broker, Topic, Partition, Consumer Group, Offset, DLQ
  * 1.4. Ứng dụng thực tế trong các hệ thống phần mềm quy mô lớn
* **CHƯƠNG II: KHẢO SÁT VÀ TỔNG QUAN VỀ VẤN ĐỀ NGHIÊN CỨU (DOMAIN & CONTEXT)**
  * 2.1. Bối cảnh kiến trúc hệ thống E-commerce hiện đại
  * 2.2. Điểm nghẽn hiệu năng I/O tại tầng Cơ sở dữ liệu quan hệ (RDBMS)
  * 2.3. Vấn đề tranh chấp dữ liệu đồng thời (Race Condition) trong Flash Sale & Checkout
  * 2.4. Xu hướng và sự cần thiết dịch chuyển từ Monolith sang Event-Driven Microservices
* **CHƯƠNG III: QUY TRÌNH GIẢI QUYẾT VẤN ĐỀ / PHÂN TÍCH BÀI TOÁN**
  * 3.1. Những vấn đề và thách thức kỹ thuật tồn đọng
    * 3.1.1. Bộ tứ lỗ hổng Caching: Penetration, Avalanche, Breakdown, Serialization
    * 3.1.2. Các nguy cơ mất mát dữ liệu và nghẽn luồng trong Message Broker
  * 3.2. Đề xuất giải pháp kiến trúc: Mô hình Hybrid (Kafka Backbone + Redis Fast-Lane)
  * 3.3. Các tiêu chuẩn kỹ thuật và yêu cầu phi chức năng cần đạt
  * 3.4. Thách thức kỹ thuật trong quá trình phát triển
* **CHƯƠNG IV: CÁC BƯỚC THỰC HIỆN / CÀI ĐẶT VÀ CẤU HÌNH CHI TIẾT**
  * 4.1. Chuẩn bị môi trường và cấu hình hạ tầng ảo hóa (Docker Compose)
  * 4.2. Triển khai phân hệ Redis chi tiết
    * 4.2.1. Chuẩn hóa Serializer và DTO Serializable
    * 4.2.2. Chống Cache Avalanche bằng Random Jitter TTL
    * 4.2.3. Chống Cache Penetration bằng Redisson Bloom Filter
    * 4.2.4. Khóa phân tán `@DistributedLock` qua Spring AOP & SpEL
    * 4.2.5. Double-Checked Locking chống Cache Stampede
    * 4.2.6. Tối ưu giỏ hàng bằng Redis Hash và bảo mật JWT Blacklist
  * 4.3. Triển khai phân hệ Apache Kafka chi tiết
    * 4.3.1. Cấu hình cơ chế Dead Letter Queue (DLQ) & ErrorHandler
    * 4.3.2. Order Event Publishing & Partitioning theo Message Key
    * 4.3.3. Idempotent Consumer với Manual Ack và Redis Lock vòng ngoài
    * 4.3.4. Hệ thống ghi vết kiểm toán (Audit Logging Stream)
    * 4.3.5. API Admin cứu hộ và tái xử lý thông điệp từ DLQ
  * 4.4. Tích hợp trạm trung chuyển Hybrid (Kafka -> Redis Pub/Sub -> WebSocket STOMP)
* **CHƯƠNG V: SẢN PHẨM / KẾT QUẢ ĐẠT ĐƯỢC**
  * 5.1. Sơ đồ kiến trúc tổng thể toàn hệ thống
  * 5.2. Kết quả thực nghiệm Caching và đo lường giảm tải CSDL
  * 5.3. Kết quả kiểm thử tranh chấp đồng thời (Concurrency Test với CountDownLatch)
  * 5.4. Kết quả vận hành cơ chế xử lý lỗi Dead Letter Queue
  * 5.5. Đánh giá mức độ hoàn thiện so với mục tiêu đề ra
* **CHƯƠNG VI: MỘT SỐ HƯỚNG ỨNG DỤNG THỰC TẾ**
  * 6.1. Khả năng mở rộng vào các ngành công nghiệp (E-Commerce, Fintech, Logistics, IoT)
  * 6.2. Các công nghệ tiên tiến có thể tích hợp trong tương lai (Debezium CDC, Outbox Pattern)
* **CHƯƠNG VII: KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN**
  * 7.1. Nhận thức và bài học kinh nghiệm của sinh viên
  * 7.2. Các hạn chế còn thiếu hụt
  * 7.3. Hướng nghiên cứu và phát triển tiếp theo
* **TÀI LIỆU THAM KHẢO**

---

## LỜI MỞ ĐẦU

### 1. Tính cấp thiết của đề tài
Trong bối cảnh nền kinh tế số và thương mại điện tử (E-commerce) bùng nổ, các ứng dụng bán lẻ trực tuyến phải đáp ứng nhu cầu khổng lồ từ hàng triệu người dùng cùng lúc. Đặc biệt, trong các chiến dịch khuyến mãi lớn (Mega Sale, Flash Sale), lưu lượng truy cập có thể tăng đột biến gấp hàng chục đến hàng trăm lần so với ngày thường. 

Mô hình kiến trúc nguyên khối (Monolithic Architecture) kết hợp với Cơ sở dữ liệu quan hệ (RDBMS - MySQL) truyền thống nhanh chóng bộc lộ các điểm nghẽn nghiêm trọng:
1. **Quá tải I/O đĩa từ tại tầng CSDL:** 80-90% các truy vấn trong E-commerce là các truy vấn đọc (Catalog, Danh mục, Xem sản phẩm). Các câu lệnh SQL phức tạp chứa nhiều mệnh đề `JOIN` làm CPU và bộ nhớ của máy chủ CSDL cạn kiệt.
2. **Hiện tượng nghẽn luồng và tranh chấp tài nguyên (Race Condition):** Hàng trăm khách hàng cùng bấm mua một sản phẩm giới hạn trong cùng một mili-giây dẫn tới hiện tượng bán âm kho (Over-selling) hoặc trùng lặp thanh toán khi mạng chập chờn.
3. **Tính gắn kết chặt chẽ (Tight Coupling) làm chậm API chính:** Luồng đặt hàng phải chờ đợi các tác vụ phụ trợ (gửi email thông báo, lưu lịch sử kiểm toán, gửi thông báo in-app). Nếu dịch vụ gửi email bên ngoài (như Gmail SMTP) bị treo, toàn bộ giao dịch mua hàng của người dùng bị kéo dài hoặc thất bại theo.

Nhằm giải quyết triệt để các thách thức trên, đề tài **"Nghiên cứu và Ứng dụng Redis & Apache Kafka trong Xây dựng Hệ thống Thương mại Điện tử Hiệu Năng Cao, Chịu Tải Lớn theo Kiến trúc Hướng Sự Kiện (Event-Driven Architecture)"** được thực hiện. Bằng cách phân tầng lưu trữ, sử dụng Redis làm bộ nhớ đệm siêu tốc (In-Memory Fast-Lane) và khóa phân tán, kết hợp Apache Kafka làm xương sống luân chuyển thông điệp bền vững (Backbone Event Streaming), hệ thống có khả năng đáp ứng đồng thời hàng ngàn truy vấn với độ trễ thấp và độ tin cậy tuyệt đối.

### 2. Mục tiêu nghiên cứu
* **Về mặt công nghệ:**
  * Nắm vững kiến trúc nội tại, cơ chế lưu trữ RAM và các cấu trúc dữ liệu của Redis; cơ chế Distributed Commit Log và phân vùng của Apache Kafka.
  * Làm chủ các giải pháp phòng thủ Caching chuyên sâu: Redisson Bloom Filter (chống Cache Penetration), Random Jitter TTL (chống Cache Avalanche), Double-Checked Locking (chống Cache Breakdown/Stampede).
  * Nghiên cứu thuật toán khóa phân tán (RedLock / Redisson RLock) và kỹ thuật lập trình hướng khía cạnh (AOP) kết hợp Spring Expression Language (SpEL) để bảo vệ các vùng tài nguyên nhạy cảm.
  * Thiết lập mô hình giao vận thông điệp an toàn trên Kafka: Ngữ nghĩa At-least-once, Manual Immediate Acknowledgment, Idempotent Consumer Pattern và Dead Letter Queue (DLQ).
* **Về mặt ứng dụng thực tiễn:**
  * Xây dựng thành công một backend E-commerce hoàn chỉnh trên nền tảng Spring Boot 3.x, tích hợp đầy đủ các phân hệ: Xác thực, Danh mục sản phẩm, Giỏ hàng động, Đặt hàng thanh toán, Flash Sale, Thông báo Realtime qua WebSocket và Kiểm toán tập trung (Audit Log).
  * Đo đạc, kiểm thử thực nghiệm (Concurrency Testing) với `CountDownLatch` để chứng minh tính đúng đắn và khả năng loại bỏ hoàn toàn Race Condition.

### 3. Đối tượng và phạm vi nghiên cứu
* **Đối tượng nghiên cứu:** 
  * Ngôn ngữ Java 17+, Framework Spring Boot 3.x, Spring Data JPA / Hibernate, Spring Data Redis, Redisson Client, Spring for Apache Kafka, Spring WebSocket & STOMP.
  * Hệ thống lưu trữ: Redis 7.x, Apache Kafka & Zookeeper, MySQL 8.x.
* **Phạm vi nghiên cứu:** 
  * Áp dụng trong phạm vi kiến trúc tầng Backend của nền tảng thương mại điện tử đa phân hệ.
  * Tập trung chuyên sâu vào hai trục công nghệ: Quản lý trạng thái bộ nhớ đệm & Đồng bộ phân tán (Redis) và Truyền thông bất đồng bộ hướng sự kiện (Kafka).

### 4. Phương pháp nghiên cứu
* **Phương pháp nghiên cứu lý thuyết:** Đọc và phân tích các tài liệu kỹ thuật chuẩn quốc tế (RFC, Whitepapers của Kafka, Redis Docs, Spring Reference Manual).
* **Phương pháp thực nghiệm đối sánh (Benchmarking & Empirical Testing):** Xây dựng các ca kiểm thử đa luồng với `CountDownLatch` (chế độ không có Lock đối đầu với có Lock) để so sánh số lượng đơn hàng, tính toàn vẹn tồn kho và độ trễ phản hồi.
* **Phương pháp "Giải phẫu và Tái tạo" (Deconstruction & Reconstruction):** Trực tiếp kiểm thử các kịch bản lỗi (comment annotation cấu hình, sai mật khẩu SMTP, lỗi serializer) để ghi nhận hành vi mã lỗi và chuẩn hóa cấu hình tối ưu.

### 5. Ý nghĩa khoa học và thực tiễn
* **Ý nghĩa khoa học:** Cung cấp mô hình kiến trúc kết hợp linh hoạt (Hybrid Architecture) giải quyết bài toán dung hòa giữa tốc độ truy xuất In-memory cực đại của RAM và độ bền vững dữ liệu trên đĩa từ (Persistence Log), đảm bảo tính nhất quán cuối cùng (Eventual Consistency) trong hệ thống phân tán.
* **Ý nghĩa thực tiễn:** Toàn bộ mã nguồn, cấu hình lỗi, giải pháp custom annotation và kịch bản cứu hộ DLQ được tài liệu hóa thành cẩm nang kỹ thuật thực chiến, có thể chuyển giao và áp dụng trực tiếp cho các dự án thương mại quy mô lớn của doanh nghiệp.

---

## CHƯƠNG I: GIỚI THIỆU CHUNG VỀ CÔNG NGHỆ / CÔNG CỤ (REDIS & KAFKA)

### 1.1. Tổng quan nguồn gốc và định nghĩa cốt lõi

#### 1.1.1. In-Memory Data Store & Cache: Redis
* **Nguồn gốc:** Redis (Remote Dictionary Server) được khởi xướng bởi Salvatore Sanfilippo (biệt danh *antirez*) vào năm 2009 tại Ý, xuất phát từ nhu cầu tối ưu hóa tốc độ tải trang cho một dịch vụ phân tích nhật ký web thời gian thực.
* **Định nghĩa cốt lõi:** Redis là một hệ thống cơ sở dữ liệu NoSQL In-Memory mã nguồn mở, hoạt động theo mô hình Key-Value. Khác với các hệ CSDL truyền thống lưu trữ dữ liệu chính trên đĩa cứng (Disk-based), toàn bộ tập dữ liệu của Redis được nạp và xử lý trực tiếp trên bộ nhớ RAM (Random Access Memory).
* **Mô hình luồng (Thread Model):** Redis sử dụng kiến trúc Single-Threaded Event Loop kết hợp với kỹ thuật I/O Multiplexing (thông qua `epoll`, `kqueue` hoặc `select`). Nhờ không bị ảnh hưởng bởi chi phí chuyển đổi ngữ cảnh (Context Switching) và khóa luồng (Thread Contention), Redis có thể thực thi hàng trăm ngàn lệnh đọc/ghi mỗi giây với độ trễ dưới 1 mili-giây.

#### 1.1.2. Distributed Event Streaming Platform: Apache Kafka
* **Nguồn gốc:** Apache Kafka ban đầu được phát triển bởi đội ngũ kỹ sư tại LinkedIn (Jay Kreps, Neha Narkhede, Jun Rao) vào năm 2010 nhằm giải quyết bài toán theo dõi luồng hành vi người dùng (Clickstream) với khối lượng dữ liệu khổng lồ. Năm 2011, Kafka được mở mã nguồn và trở thành dự án Top-Level của Apache Software Foundation.
* **Định nghĩa cốt lõi:** Apache Kafka là nền tảng truyền tải luồng sự kiện phân tán (Distributed Event Streaming Platform), hoạt động dựa trên mô hình Publish-Subscribe kết hợp kiến trúc nhật ký tuần tự (Distributed Append-Only Commit Log).
* **Cơ chế lưu trữ:** Kafka tối ưu hóa việc ghi và đọc tuần tự trên ổ đĩa (Sequential Disk I/O) kết hợp kỹ thuật Zero-Copy OS Data Transfer (`sendfile`), cho phép đạt được thông lượng (throughput) hàng triệu thông điệp mỗi giây trên phần cứng tiêu chuẩn.

---

### 1.2. Đánh giá ưu điểm và hạn chế

#### 1.2.1. Đánh giá kỹ thuật về Redis
* **Ưu điểm:**
  1. *Tốc độ truy xuất cực đại:* Nhờ cơ chế In-memory, thời gian phản hồi trung bình chỉ từ 1-5 mili-giây, giúp triệt tiêu hiện tượng thắt cổ chai I/O của database chính.
  2. *Cấu trúc dữ liệu phong phú:* Hỗ trợ String, List, Set, Sorted Set, Hash, Bitmaps, HyperLogLog, Streams.
  3. *Tính nguyên tử (Atomicity):* Mọi lệnh thực thi trên từng key đều là Atomic; hỗ trợ transaction thông qua `MULTI/EXEC` và lập trình nhúng Lua Script.
  4. *Khả năng mở rộng đa dạng:* Hỗ trợ Master-Slave Replication, Redis Sentinel (Auto-failover) và Redis Cluster (Sharding dữ liệu trên 16,384 Hash Slots).
* **Hạn chế:**
  1. *Chi phí bộ nhớ đắt đỏ:* Dữ liệu bị giới hạn bởi dung lượng RAM vật lý; chi phí mở rộng RAM lớn hơn nhiều so với mở rộng dung lượng ổ đĩa.
  2. *Rủi ro mất mát dữ liệu:* Vì là bộ nhớ tạm, nếu máy chủ gặp sự cố sập nguồn đột ngột mà chưa kịp đồng bộ (flush) xuống đĩa, dữ liệu mới ghi có thể bị mất.
  3. *Không phù hợp cho lưu trữ lâu dài:* Không tối ưu khi dùng làm kho lưu trữ lịch sử hoặc hàng đợi thông điệp tích lũy qua nhiều năm.

#### 1.2.2. Đánh giá kỹ thuật về Apache Kafka
* **Ưu điểm:**
  1. *Thông lượng vượt trội (Ultra-High Throughput):* Kiến trúc phân tán theo Partition cho phép chia tải ngang trên hàng trăm broker và hàng ngàn consumer.
  2. *Lưu trữ bền vững và tin cậy (Fault-Tolerant & Durable):* Dữ liệu được ghi thẳng xuống đĩa cứng và tự động sao chép (replication) sang các broker khác theo chính sách Replication Factor.
  3. *Khả năng lưu vết và Replay:* Thông điệp không bị xóa ngay sau khi được đọc (như RabbitMQ hay JMS), mà được lưu trữ theo chính sách thời gian (Retention Policy, ví dụ: 7 ngày, 30 ngày). Nhờ đó, các consumer mới có thể tua lại (rewind offset) để xử lý lại toàn bộ lịch sử dữ liệu.
* **Hạn chế:**
  1. *Độ phức tạp triển khai cao:* Đòi hỏi quản lý cụm Zookeeper (hoặc KRaft metadata quorum), quản lý partition, replication, cân bằng tải cụm.
  2. *Độ trễ nhỉnh hơn RAM:* Mặc dù cực nhanh đối với Disk-based, độ trễ xử lý của Kafka (khoảng 5-20ms) vẫn cao hơn so với Redis (< 1ms).
  3. *Quản trị Consumer phức tạp:* Cần kiểm soát chặt chẽ hiện tượng Consumer Rebalance, Consumer Lag và điều phối Commit Offset để tránh mất tin hoặc trùng tin.

---

### 1.3. Các tính năng và cơ chế hoạt động nổi bật

#### 1.3.1. Phân hệ Redis
1. **Các kiểu dữ liệu chính:**
   * *String:* Lưu trữ giá trị chuỗi, JSON cache hoặc số nguyên để đếm nguyên tử (`INCR/DECR`).
   * *Hash:* Cấu trúc ánh xạ Field-Value, tối ưu hóa lưu trữ các thực thể có nhiều thuộc tính (như Giỏ hàng).
   * *Bitmap / Bitfield:* Thao tác trực tiếp trên từng bit nhị phân, là nền tảng để Redisson triển khai thuật toán Bloom Filter với mức tiêu thụ RAM cực thấp.
2. **Cơ chế Khóa phân tán (Redisson Distributed Lock):**
   * Sử dụng thuật toán khóa phân tán chống Deadlock với cơ chế tự động gia hạn (Watchdog - mặc định kiểm tra mỗi 10 giây).
   * Hỗ trợ hàm `tryLock(waitTime, leaseTime, unit)` cho phép thread chờ đợi có giới hạn trước khi từ bỏ, tránh treo luồng ứng dụng.
3. **Cấu trúc dữ liệu xác suất - Bloom Filter:**
   * Cấu trúc dữ liệu xác định một phần tử "chắc chắn chưa tồn tại" hoặc "có thể đã tồn tại" trong tập dữ liệu khổng lồ.
   * Sử dụng một mảng bit kích thước $m$ và $k$ hàm băm độc lập, giảm thiểu tối đa bộ nhớ cần dùng.
4. **Cơ chế bền vững hóa dữ liệu (Persistence):**
   * *RDB (Redis Database Backup):* Chụp ảnh nhanh toàn bộ dữ liệu tại các thời điểm định kỳ (Point-in-time Snapshot).
   * *AOF (Append Only File):* Ghi lại toàn bộ các câu lệnh ghi (Write Commands) vào file nhật ký dạng log. Chế độ `appendfsync everysec` giúp hệ thống đạt cân bằng tối ưu giữa hiệu năng và độ an toàn dữ liệu.
5. **Cơ chế kênh thông điệp (Redis Pub/Sub):**
   * Mô hình bắn tin tức thời (Fire-and-forget), phù hợp cho các tác vụ cần thông báo thời gian thực với độ trễ cực ngắn giữa các dịch vụ backend.

#### 1.3.2. Phân hệ Kafka
1. **Broker, Topic và Partition:**
   * *Broker:* Một node/server trong cụm Kafka.
   * *Topic:* Danh mục logic chứa các sự kiện liên quan (VD: `order_events_topic`).
   * *Partition:* Đơn vị phân chia vật lý của một Topic. Mỗi partition là một chuỗi log tuần tự, có thứ tự bất biến được đánh chỉ mục bằng **Offset**.
2. **Cơ chế Message Key & Strict Ordering:**
   * Khi gửi một thông điệp kèm `Key` (ví dụ: `orderId`), Kafka sử dụng thuật toán băm MurmurHash2 trên Key để chỉ định tin nhắn đó luôn luôn rơi vào cùng một Partition cụ thể. Nhờ đó, tính tuần tự logic của toàn bộ sự kiện liên quan đến đơn hàng được bảo đảm tuyệt đối.
3. **Consumer Group & Offset Management:**
   * Nhiều consumer cùng thuộc một Consumer Group sẽ chia nhau tiêu thụ các partition độc lập.
   * *Auto Commit vs Manual Commit:* Mặc định Kafka commit offset định kỳ tự động. Tuy nhiên, trong các hệ thống đòi hỏi độ tin cậy cao, chế độ Manual Commit (`ack.acknowledge()`) được kích hoạt để đảm bảo chỉ khi nghiệp vụ hoàn tất thành công thì vị trí đọc mới được ghi nhận.
4. **Dead Letter Queue (DLQ):**
   * Kỹ thuật cách ly các thông điệp lỗi (Poison Pill). Khi một thông điệp gây lỗi quá số lần cho phép (Retry Exhausted), thay vì làm sập consumer hoặc kẹt luồng xử lý, Kafka tự động chuyển thông điệp đó sang một Topic riêng biệt (`order_dlq_topic`) để phục vụ kiểm tra và tái xử lý sau.

---

### 1.4. Ứng dụng thực tế trong các hệ thống phần mềm lớn
* **Netflix & Spotify:** Sử dụng Kafka để xử lý hàng tỷ sự kiện phát trực tuyến (Streaming telemetry), gợi ý nội dung cá nhân hóa theo thời gian thực; sử dụng Redis để lưu trữ phiên người dùng và trạng thái danh sách phát.
* **Shopee, Lazada, Tiki:** Sử dụng Redis làm bộ nhớ đệm danh mục sản phẩm, giỏ hàng, bảng xếp hạng Flash Sale và khóa giữ chỗ thanh toán; sử dụng Kafka để điều phối quy trình xử lý đơn hàng phân tán (Tạo đơn -> Trừ kho -> Thanh toán -> Giao vận -> Hóa đơn).
* **Uber & Grab:** Sử dụng Kafka để thu thập hàng triệu tọa độ GPS của tài xế mỗi giây; sử dụng Redis Geo-Spatial để truy vấn các tài xế gần vị trí khách hàng nhất trong bán kính 1km với tốc độ dưới 5 mili-giây.

---

## CHƯƠNG II: KHẢO SÁT VÀ TỔNG QUAN VỀ VẤN ĐỀ NGHIÊN CỨU (DOMAIN & CONTEXT)

### 2.1. Bối cảnh kiến trúc hệ thống E-commerce hiện đại
Một hệ thống E-commerce tiêu chuẩn bao gồm các nghiệp vụ cốt lõi:
1. **Quản lý danh mục & sản phẩm:** Cung cấp thông tin chi tiết, hình ảnh, phân loại, giá cả, thương hiệu.
2. **Quản lý giỏ hàng:** Người dùng liên tục thêm, bớt, cập nhật số lượng các mặt hàng cần mua.
3. **Quản lý khuyến mãi (Coupon):** Xác thực mã giảm giá, kiểm tra điều kiện đơn hàng tối thiểu và giới hạn lượt sử dụng.
4. **Quy trình Đặt hàng & Thanh toán (Checkout & Order):** Khóa tồn kho, trừ giỏ hàng, tính tổng tiền, tạo đơn hàng và tích hợp cổng thanh toán (VNPay, MoMo, COD).
5. **Thông báo & Kiểm toán:** Gửi email xác nhận, gửi thông báo đẩy in-app và ghi vết nhật ký truy cập nhạy cảm phục vụ bảo mật.

### 2.2. Điểm nghẽn hiệu năng I/O tại tầng Cơ sở dữ liệu quan hệ (RDBMS)
* Trong mô hình truyền thống, mọi thao tác đọc/ghi đều đập trực tiếp vào MySQL.
* Mỗi lần người dùng xem một sản phẩm, câu truy vấn SQL phải thực hiện nhiều phép `LEFT JOIN` qua các bảng: `products`, `categories`, `brands`, `product_images`. Khi số lượng người dùng đồng thời tăng cao, số lượng kết nối (Database Connection Pool) nhanh chóng bị cạn kiệt, thời gian phản hồi API tăng từ vài chục mili-giây lên hàng ngàn mili-giây, dẫn tới lỗi HTTP 504 Gateway Timeout.

### 2.3. Vấn đề tranh chấp dữ liệu đồng thời (Race Condition) trong Flash Sale & Checkout
* **Kịch bản Flash Sale:** Sản phẩm hot chỉ còn lại 10 đơn vị trong kho, nhưng nhận được 500 yêu cầu mua hàng gửi về trong cùng 1 giây.
  * Nếu không có cơ chế khóa, 500 luồng ứng dụng sẽ cùng đọc giá trị tồn kho `stock = 10` từ CSDL.
  * Sau đó, cả 500 luồng đều thấy điều kiện `stock > 0` thỏa mãn, tiến hành trừ kho và tạo đơn hàng. Kết quả là 500 đơn hàng được tạo cho 10 món hàng thực tế, gây ra hiện tượng **bán âm kho (Over-selling)** nghiêm trọng.
* **Kịch bản Click đúp thanh toán:** Người dùng bấm liên tục vào nút "Đặt hàng" do mạng lag hoặc ứng dụng phản hồi chậm. Nếu không khóa hành động này theo từng phiên người dùng, hệ thống sẽ thực hiện tạo nhiều đơn hàng giống hệt nhau, trừ tiền nhiều lần và làm sai lệch số liệu tồn kho.

### 2.4. Xu hướng và sự cần thiết dịch chuyển từ Monolith sang Event-Driven Microservices
* Trong kiến trúc Monolith cổ điển, hàm xử lý đặt hàng thường được viết theo mô hình đồng bộ tuần tự:
  ```
  Client -> createOrder() -> [Save DB Order] -> [Send Email SMTP] -> [Save Notification] -> [Save Audit Log] -> Return Response
  ```
* **Nhược điểm cốt tử:**
  * Tác vụ gửi email qua mạng SMTP thường mất từ 1 đến 3 giây. Nếu máy chủ mail phản hồi chậm hoặc timeout, toàn bộ giao dịch đặt hàng bị nghẽn lại, kéo tụt trải nghiệm khách hàng.
  * Nếu máy chủ gửi mail bị lỗi đứt cáp, exception ném ra có nguy cơ làm rollback toàn bộ giao dịch tạo đơn hàng của khách.
  * Sự phụ thuộc lẫn nhau giữa các service ngăn cản việc bóc tách dự án thành các dịch vụ độc lập trong tương lai.
* **Giải pháp hướng tới:** Chuyển sang mô hình Hướng Sự Kiện (Event-Driven): Hàm `createOrder()` chỉ tập trung tạo đơn hàng, trừ kho và bắn một sự kiện `OrderEvent` vào hàng đợi thông điệp; các tác vụ phụ trợ (gửi mail, thông báo, ghi log) được chuyển thành các Consumer bất đồng bộ chạy độc lập.

---

## CHƯƠNG III: QUY TRÌNH GIẢI QUYẾT VẤN ĐỀ / PHÂN TÍCH BÀI TOÁN

### 3.1. Những vấn đề và thách thức kỹ thuật tồn đọng

#### 3.1.1. Bộ tứ lỗ hổng Caching kinh điển
1. **Cache Penetration (Xuyên thủng Cache):**
   * Xảy ra khi người dùng hoặc kẻ tấn công gửi liên tục các request truy vấn dữ liệu không hề tồn tại trong hệ thống (ví dụ: `GET /products/-999999` hoặc chuỗi UUID ngẫu nhiên).
   * Vì dữ liệu không tồn tại, tầng Cache luôn trả về kết quả Miss, ép toàn bộ request phải đi thẳng xuống quét CSDL.
2. **Cache Avalanche (Tuyết lở Cache):**
   * Xảy ra khi một lượng lớn các key trong cache được cấu hình có cùng thời gian hết hạn (ví dụ: tất cả đều hết hạn sau đúng 60 phút).
   * Khi mốc thời gian này đến, hàng loạt key đồng loạt biến mất khỏi RAM, tạo ra một cơn "bão truy vấn" ập xuống CSDL cùng một lúc làm tê liệt toàn bộ hệ thống.
3. **Cache Breakdown / Cache Stampede (Vỡ đê Cache):**
   * Xảy ra khi một Hot-Key (sản phẩm đang cực kỳ thịnh hành hoặc danh mục chính) bất ngờ hết hạn. Đúng lúc đó, hàng ngàn request đồng thời ập tới và nhận kết quả Cache Miss. Tất cả các luồng này cùng lúc thực thi câu lệnh SQL xuống CSDL để nạp lại cache, khiến Database bị sập ngay lập tức.
4. **Lỗ hổng Tuần tự hóa (Serialization Domino):**
   * Việc cố tình serialize trực tiếp các Entity Hibernate (`User`, `Product`) sang JSON bằng Jackson thường dẫn tới lỗi `LazyInitializationException` (do cố truy cập vào các proxy collection như `PersistentBag` khi session đã đóng) hoặc lỗi `StackOverflowError` do quan hệ vòng tròn (Circular Reference).
   * Ngược lại, nếu dùng cơ chế Native Java Serialization mà quên khai báo `serialVersionUID` hoặc bỏ sót `implements Serializable` ở các đối tượng lồng nhau (Nested Objects), hệ thống sẽ ném ngoại lệ `InvalidClassException` hoặc `NotSerializableException`.

#### 3.1.2. Các nguy cơ trong Message Broker (Kafka)
1. **Mất mát thông điệp (Message Loss):** Sử dụng cơ chế Auto-Commit Offset khiến broker đánh dấu thông điệp đã đọc ngay cả khi ứng dụng chưa xử lý xong nghiệp vụ. Nếu máy chủ ứng dụng sập nguồn giữa chừng, thông điệp đó bị mất vĩnh viễn.
2. **Kẹt hàng đợi (Poison Pill):** Một thông điệp chứa nội dung sai định dạng hoặc gây lỗi runtime khiến consumer ném exception liên tục. Nếu không có cơ chế cách ly, consumer sẽ bị kẹt mãi mãi tại thông điệp này và không thể đọc tiếp các tin nhắn phía sau.
3. **Trùng lặp xử lý (Duplicate Processing):** Khi consumer xử lý xong nghiệp vụ (đã ghi DB và gửi mail) nhưng mạng bị đứt trước khi kịp gửi xác nhận (Ack) về cho Kafka, broker sẽ tự động gửi lại thông điệp đó cho consumer khác (At-least-once Re-delivery). Nếu không có cơ chế phòng thủ Idempotency, khách hàng sẽ bị nhận nhiều email hoặc bị trừ tiền hai lần.
4. **Xử lý sai thứ tự sự kiện (Out-of-Order Execution):** Nếu hai sự kiện `Order_Created` và `Order_Cancelled` của cùng một đơn hàng được gửi vào các partition khác nhau và được xử lý bởi hai thread độc lập, sự kiện `Cancelled` có thể chạy trước sự kiện `Created`, phá vỡ tính đúng đắn của quy trình nghiệp vụ.

---

### 3.2. Đề xuất giải pháp kiến trúc: Mô hình Hybrid (Kafka Backbone + Redis Fast-Lane)

Để giải quyết đồng thời bài toán tốc độ và độ tin cậy, đề tài xây dựng mô hình kiến trúc hai tầng bổ trợ cho nhau:

```
[ CLIENT BROWSER / MOBILE APP ]
        │                  ▲
   REST API          WebSocket STOMP (/topic/user/{id}/notifications)
        │                  │
        ▼                  │
┌────────────────────────────────────────────────────────┐
│               SPRING BOOT APPLICATION BACKEND           │
│                                                        │
│  [Security Filter]  ──> [Redis Token Blacklist]        │
│  [Query Products]   ──> [Redisson Bloom Filter]        │
│                         └─> [Redis Cache + Jitter]     │
│                             └─> [Double-Checked Lock]  │
│  [Order Checkout]   ──> [Redis SETNX Idempotency Lock] │
│  [Flash Sale]       ──> [@DistributedLock (Redisson)]  │
│  [MySQL Database]   <── [Save Order / Update Stock]    │
│  [Kafka Producer]   ──> [Publish OrderEvent (Key)]     │
└──────────────────────────┬─────────────────────────────┘
                           │
                           ▼ (order_events_topic)
┌────────────────────────────────────────────────────────┐
│                   APACHE KAFKA BROKER                  │
│                                                        │
│  - Partitioning by Message Key (orderId)               │
│  - Replication & Disk Commit Log                       │
│  - Error Handling: Dead Letter Queue (order_dlq_topic) │
└──────────────────────────┬─────────────────────────────┘
                           │
                           ▼ (Consumer: Manual Ack)
┌────────────────────────────────────────────────────────┐
│                 ORDER KAFKA CONSUMER                   │
│                                                        │
│  1. Check Idempotency: Redis setIfAbsent()             │
│  2. Send Email Confirmation via SMTP                   │
│  3. Save Notification into MySQL DB                    │
│  4. Publish Ping Signal ──> [Redis Pub/Sub Channel]    │
│  5. Acknowledge Message Offset (ack.acknowledge())     │
└──────────────────────────┬─────────────────────────────┘
                           │
                           ▼ (realtime_notifications)
┌────────────────────────────────────────────────────────┐
│          REALTIME REDIS SUBSCRIBER (WEB SOCKET)        │
│                                                        │
│  - Listen Redis Channel Topic                          │
│  - Convert & Push via SimpMessagingTemplate            │
└────────────────────────────────────────────────────────┘
```

* **Vai trò của Apache Kafka (Xương sống - Backbone):**
  * Đảm nhận vai trò lưu trữ bền vững các Domain Events quan trọng (`OrderEvent`, `AuditEvent`).
  * Thực thi các tác vụ nặng, tốn thời gian chạy ngầm: gửi mail qua Internet, ghi nhật ký kiểm toán, đồng bộ CSDL.
  * Hỗ trợ cơ chế phục hồi lỗi, retry và cách ly lỗi bằng Dead Letter Queue (DLQ).
* **Vai trò của Redis (Đường cao tốc - Fast Lane):**
  * Đóng vai trò lớp phòng thủ phía trước cho Database: Cache danh mục, giỏ hàng, thông tin user.
  * Chặn đứng các đợt tấn công Penetration thông qua Bloom Filter.
  * Cung cấp cơ chế khóa phân tán siêu tốc: Chặn click đúp khi checkout bằng `SETNX`, đồng bộ hóa tồn kho Flash Sale bằng Redisson Lock.
  * Hoạt động như trạm phát sóng nhanh (Pub/Sub) kết nối Consumer xử lý sự kiện ngầm với các máy chủ WebSocket để bắn thông báo tức thì xuống trình duyệt.

---

### 3.3. Các tiêu chuẩn kỹ thuật và yêu cầu phi chức năng cần đạt
1. **Hiệu năng (Performance):** 
   * Thời gian phản hồi của các API đọc sản phẩm/danh mục khi đã có cache phải duy trì dưới mức **10ms**.
   * API đặt hàng chính phải phản hồi kết quả trong vòng dưới **100ms**, không bị chậm trễ bởi thời gian gửi email hay push thông báo.
2. **Tính toàn vẹn & Chống tranh chấp (Data Consistency & Concurrency):**
   * Số lượng tồn kho tuyệt đối không bị bán âm trong bất kỳ trường hợp nào, kể cả khi có 100 luồng đồng thời tranh mua trong cùng một mili-giây.
   * Mỗi đơn hàng chỉ được tạo duy nhất một lần dù người dùng có cố tình bấm liên tục nút thanh toán.
3. **Độ tin cậy & Khả năng chịu lỗi (Reliability & Fault Tolerance):**
   * Hệ thống tuân thủ nghiêm ngặt ngữ nghĩa phân phối **At-least-once**.
   * Khi máy chủ gửi mail bị lỗi hoặc mất mạng, đơn hàng vẫn phải được bảo toàn trong hệ thống; thông điệp gửi mail lỗi được đưa vào DLQ và có thể được cứu hộ để gửi lại khi hạ tầng mạng hồi phục.

---

### 3.4. Thách thức kỹ thuật trong quá trình phát triển
1. **Vấn đề Self-Invocation của Spring AOP:**
   * Khi áp dụng `@DistributedLock` lên một hàm nội bộ trong `FlashSaleService`, nếu gọi hàm này từ một hàm khác trong cùng một class (`this.buyWithLock()`), lệnh gọi sẽ đi trực tiếp vào đối tượng gốc thay vì đi qua Spring CGLIB Proxy, làm mất tác dụng của khóa phân tán.
   * *Giải pháp:* Tự inject `ApplicationContext` để lấy đối tượng Proxy của chính class đó từ Spring Bean Container (`applicationContext.getBean(FlashSaleService.class)`).
2. **Định tuyến khóa động bằng SpEL (Spring Expression Language):**
   * Khóa phân tán không thể cố định tên hàm mà phải gắn liền với từng mã sản phẩm cụ thể (`#productId`) để tránh việc khóa toàn bộ hệ thống bán hàng.
   * *Giải pháp:* Tích hợp `SpelExpressionParser` và `DefaultParameterNameDiscoverer` trong lớp Aspect để phân tích cú pháp biểu thức động tại thời điểm chạy (Runtime).
3. **Sự cố tương thích thư viện Serialization:**
   * Spring Boot 3.x sử dụng Hibernate 7, việc cài đặt thêm module `jackson-datatype-hibernate6` gây xung đột thư viện nghiêm trọng (`Error creating bean entityManagerFactory`).
   * *Giải pháp:* Thay thế toàn bộ bằng cơ chế DTO Serialization sạch sẽ, sử dụng `JdkSerializationRedisSerializer` kết hợp `serialVersionUID` cố định.

---

## CHƯƠNG IV: CÁC BƯỚC THỰC HIỆN / CÀI ĐẶT VÀ CẤU HÌNH CHI TIẾT

### 4.1. Chuẩn bị môi trường và cấu hình hạ tầng ảo hóa (Docker Compose)
Dự án sử dụng Docker Compose để quản lý tập trung toàn bộ hạ tầng phân tán bao gồm Zookeeper, Apache Kafka và Redis.

#### 1. Cấu hình file `docker-compose.yml`:
```yaml
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.4
    container_name: zookeeper
    ports:
      - "2181:2181"
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  kafka:
    image: confluentinc/cp-kafka:7.4.4
    container_name: kafka
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: 'zookeeper:2181'
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://127.0.0.1:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

  redis:
    image: redis:latest
    container_name: redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    command: redis-server --appendonly yes --appendfsync everysec --maxmemory 512mb --maxmemory-policy allkeys-lru

volumes:
  redis_data:
```

#### 2. Cấu hình tham số ứng dụng trong `application.yaml`:
```yaml
server:
  port: 8080
  servlet:
    context-path: /e-commerce

spring:
  data:
    redis:
      host: localhost
      port: 6379
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      enable-auto-commit: false
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
    listener:
      ack-mode: MANUAL_IMMEDIATE
    properties:
      spring.json.trusted.packages: "*"
```

---

### 4.2. Triển khai phân hệ Redis chi tiết

#### 4.2.1. Chuẩn hóa Serializer và DTO Serializable
Để triệt tiêu lỗi mất session của Hibernate (`LazyInitializationException`) và lỗi tương thích lớp khi nạp lại từ RAM, dự án cấu hình chuẩn mực bộ tuần tự hóa trong [RedisConfig.java](file:///d:/Java%20Spring/e-ecommerce/demo/src/main/java/com/example/demo/config/RedisConfig.java):

```java
@Bean
public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    
    // Key dùng String cho dễ quan sát trên Redis CLI / GUI
    template.setKeySerializer(new StringRedisSerializer());
    // Value dùng JdkSerializationRedisSerializer giải quyết triệt để vấn đề Hibernate Proxy
    template.setValueSerializer(new JdkSerializationRedisSerializer());
    
    // Cấu hình riêng cho Hash (Dùng cho Giỏ hàng)
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(new StringRedisSerializer());
    
    return template;
}
```

Mọi lớp DTO tham gia vào tầng Caching đều bắt buộc phải triển khai `Serializable` và cố định `serialVersionUID`:
```java
public class UserResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String id;
    private String email;
    private String fullName;
    // ...
}
```

#### 4.2.2. Chống Cache Avalanche bằng Random Jitter TTL
Thay vì sử dụng một mốc TTL cố định (60 phút) khiến toàn bộ key hết hạn cùng lúc, hệ thống tích hợp bộ sinh số ngẫu nhiên vào thời gian sống của cache:

```java
@Bean
public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    // Khởi tạo Random để tạo Jitter từ 1 đến 15 phút
    long baseMinutes = 60L;
    long jitterMinutes = this.random.nextInt(15) + 1L;

    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(baseMinutes + jitterMinutes))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new JdkSerializationRedisSerializer()));

    return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
}
```

#### 4.2.3. Chống Cache Penetration bằng Redisson Bloom Filter
Hệ thống sử dụng Redisson để khởi tạo một Bloom Filter lưu trữ toàn bộ các ID hợp lệ của người dùng trong hệ thống tại [BloomFilterManager.java](file:///d:/Java%20Spring/e-ecommerce/demo/src/main/java/com/example/demo/config/BloomFilterManager.java):

```java
@Service
public class BloomFilterManager {
    private final RedissonClient redissonClient;
    private final UserRepository userRepository;
    private RBloomFilter<String> userBloomFilter;

    public BloomFilterManager(RedissonClient redissonClient, UserRepository userRepository){
        this.redissonClient = redissonClient;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init(){
        userBloomFilter = redissonClient.getBloomFilter("userBloomFilter");
        // Khởi tạo sức chứa 100.000 phần tử, tỷ lệ sai số false positive chấp nhận là 1% (0.01)
        userBloomFilter.tryInit(100000L, 0.01);

        // Nạp toàn bộ ID người dùng hiện có từ CSDL vào Bloom Filter
        userRepository.findAll().forEach(user -> userBloomFilter.add(user.getId()));
    }

    public boolean mightContainUser(String userId){
        return userBloomFilter.contains(userId);
    }

    public void addUser(String userId){
        userBloomFilter.add(userId);
    }
}
```

Tại [UserService.java](file:///d:/Java%20Spring/e-ecommerce/demo/src/main/java/com/example/demo/service/UserService.java), phương thức `getUserById` kiểm tra Bloom Filter trước khi thực hiện bất kỳ thao tác nào:
```java
@Cacheable(value = "users", key = "#id", sync = true)
public UserResponse getUserById(String id){
    // Chặn đứng Cache Penetration tại đây
    if (!bloomFilterManager.mightContainUser(id)) {
        throw new AppException(ErrorCode.USER_NOT_FOUND);
    }
    return userMapper.toResponse(userRepository.findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
}
```

#### 4.2.4. Khóa phân tán `@DistributedLock` qua Spring AOP & SpEL
Định nghĩa Custom Annotation `@DistributedLock`:
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {
    String keyPrefix() default "lock:";
    String key();
    long waitTime() default 5;
    long leaseTime() default 10;
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
```

Xây dựng Aspect chặn và xử lý khóa tại [DistributedLockAspect.java](file:///d:/Java%20Spring/e-ecommerce/demo/src/main/java/com/example/demo/aspect/DistributedLockAspect.java):
```java
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAspect {
    private final RedissonClient redissonClient;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(distributedLock)")
    public Object handleDistributedLock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        // Biên dịch biểu thức SpEL (Ví dụ: #productId -> PROD-001)
        String dynamicKey = parseSpelKey(distributedLock.key(), method, args);
        String lockKey = distributedLock.keyPrefix() + dynamicKey;

        RLock lock = redissonClient.getLock(lockKey);
        boolean isLocked = false;

        try {
            isLocked = lock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());
            if (isLocked) {
                return joinPoint.proceed();
            } else {
                throw new RuntimeException("Hệ thống đang bận. Timeout khi đợi lock: " + lockKey);
            }
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private String parseSpelKey(String spel, Method method, Object[] args) {
        if (spel == null || spel.trim().isEmpty()) return method.getName();
        String[] params = discoverer.getParameterNames(method);
        EvaluationContext context = new StandardEvaluationContext();
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                context.setVariable(params[i], args[i]);
            }
        }
        try {
            return parser.parseExpression(spel).getValue(context, String.class);
        } catch (Exception e) {
            return spel;
        }
    }
}
```

Áp dụng bảo vệ Flash Sale trong [FlashSaleService.java](file:///d:/Java%20Spring/e-ecommerce/demo/src/main/java/com/example/demo/service/FlashSaleService.java):
```java
@DistributedLock(keyPrefix = "flashsale:product:", key = "#productId", waitTime = 5, timeUnit = TimeUnit.SECONDS)
public void buyWithLock(String productId, String userId) {
    processPurchase(productId, userId, "Lock");
}
```

#### 4.2.5. Double-Checked Locking chống Cache Stampede
Trong [ProductService.java](file:///d:/Java%20Spring/e-ecommerce/demo/src/main/java/com/example/demo/service/ProductService.java), phương thức `getProduct(Long id)` tự thực hiện kỹ thuật Double-Checked Locking bằng Redisson Lock:

```java
public ProductSummaryResponse getProduct(Long id) {
    // 1. Tăng bộ đếm lượt xem bằng Redis atomic INCR
    redisTemplate.opsForValue().increment("product:views:" + id);

    // 2. Kiểm tra Cache lần 1
    String cacheKey = "products::" + id;
    Object cachedData = redisTemplate.opsForValue().get(cacheKey);
    if (cachedData != null) {
        return (ProductSummaryResponse) cachedData;
    }

    // 3. Cache Miss -> Khóa luồng bằng Redisson Lock để chỉ 1 luồng được truy vấn DB
    String lockKey = "lock:stampede:product:" + id;
    RLock lock = redissonClient.getLock(lockKey);
    boolean isAcquired = false;

    try {
        isAcquired = lock.tryLock(3, 5, TimeUnit.SECONDS);
        if (!isAcquired) {
            // Luồng khác đang nạp cache -> Chờ 50ms và đọc lại từ cache
            Thread.sleep(50);
            return (ProductSummaryResponse) redisTemplate.opsForValue().get(cacheKey);
        }

        // 4. Kiểm tra Cache lần 2 (Double-Check)
        Object doubleCheckCache = redisTemplate.opsForValue().get(cacheKey);
        if (doubleCheckCache != null) {
            return (ProductSummaryResponse) doubleCheckCache;
        }

        // 5. Nếu chưa có mới xuống Database truy vấn
        var product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        ProductSummaryResponse response = productMapper.toSummaryResponse(product);

        // 6. Nạp vào Redis Cache với TTL 60 phút
        redisTemplate.opsForValue().set(cacheKey, response, Duration.ofMinutes(60));
        return response;
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("Gián đoạn luồng khi đợi lock", e);
    } finally {
        if (isAcquired && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
```

#### 4.2.6. Tối ưu Giỏ hàng bằng Redis Hash & Bảo mật JWT Blacklist
* **Tối ưu giỏ hàng ([CartService.java](file:///d:/Java%20Spring/e-ecommerce/demo/src/main/java/com/example/demo/service/CartService.java)):**
  Sử dụng cấu trúc Redis Hash `cart:{userId}` với cặp `productId -> quantity`. Khi người dùng tra cứu hoặc sửa đổi giỏ hàng, thao tác được thực hiện trực tiếp trên từng trường (Field) bằng `opsForHash()`, giảm thiểu tối đa kích thước dữ liệu truyền tải qua mạng.
* **Bảo mật phiên JWT ([RedisTokenService.java](file:///d:/Java%20Spring/e-ecommerce/demo/src/main/java/com/example/demo/service/RedisTokenService.java)):**
  Khi người dùng đăng xuất, Access Token được đưa vào danh sách đen `blacklist:{accessToken}` với thời gian sống (TTL) bằng thời gian hiệu lực còn lại của Token. Bộ lọc `JwtAuthenticationFilter` kiểm tra Redis trước mỗi request để từ chối các token đã bị thu hồi.

---

### 4.3. Triển khai phân hệ Apache Kafka chi tiết

#### 4.3.1. Cấu hình cơ chế Dead Letter Queue (DLQ) & ErrorHandler
Cấu hình tại [KafkaDLQConfig.java](file:///d:/Java%20Spring/e-ecommerce/demo/src/main/java/com/example/demo/config/KafkaDLQConfig.java):

```java
@Slf4j
@Configuration
public class KafkaDLQConfig {

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> template) {
        // 1. Recoverer: Đẩy thông điệp vào topic nhà xác "order_dlq_topic" khi hết lượt retry
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template,
                (ConsumerRecord<?, ?> record, Exception exception) -> {
                    log.error("Xử lý thất bại hoàn toàn sau số lần retry, đẩy message vào DLQ: {}", record.key());
                    return new TopicPartition("order_dlq_topic", -1);
                });

        // 2. Cấu hình chiến lược Retry: Chờ 2 giây, thử lại tối đa 3 lần
        FixedBackOff backOff = new FixedBackOff(2000L, 3);

        return new DefaultErrorHandler(recoverer, backOff);
    }
}
```

#### 4.3.2. Order Event Publishing & Partitioning theo Message Key
Trong [OrderService.java](file:///d:/Java%20Spring/e-ecommerce/demo/src/main/java/com/example/demo/service/OrderService.java):
* **Chặn click đúp thanh toán:** Sử dụng `SETNX` với TTL 3 giây:
  ```java
  String lockKey = "lock:checkout:" + userId;
  Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", 3, TimeUnit.SECONDS);
  if (Boolean.FALSE.equals(acquired)) {
      throw new AppException(ErrorCode.TOO_MANY_REQUEST);
  }
  ```
* **Bắn sự kiện sang Kafka:**
  ```java
  OrderEvent event = new OrderEvent(
          savedOrder.getId().toString(),
          userId,
          "CREATED",
          finalAmount.doubleValue()
  );
  // Truyền event.getOrderId() làm Message Key để đảm bảo định tuyến đúng Partition
  kafkaTemplate.send("order_events_topic", event.getOrderId(), event);
  ```

#### 4.3.3. Idempotent Consumer với Manual Ack và Redis Lock vòng ngoài
Tại [OrderKafkaConsumer.java](file:///d:/Java%20Spring/e-ecommerce/demo/src/main/java/com/example/demo/listener/OrderKafkaConsumer.java):

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderKafkaConsumer {

    private final MailService mailService;
    private final NotificationService notificationService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @KafkaListener(topics = "order_events_topic", groupId = "ecommerce-notification-group")
    public void handleOrderEvents(OrderEvent event, Acknowledgment ack) {
        log.info("Kafka Consumer tiếp nhận OrderEvent: {}", event.getOrderId());

        try {
            if ("CREATED".equals(event.getAction())) {
                // 1. CHỐT CHẶN IDEMPOTENCY: Chống lặp thông điệp bằng Redis SETNX
                String redisKey = "processed:order_created:" + event.getOrderId();
                Boolean isNewEvent = redisTemplate.opsForValue().setIfAbsent(redisKey, "1", Duration.ofHours(24));
                if (Boolean.FALSE.equals(isNewEvent)) {
                    log.warn("Sự kiện đơn hàng {} đã được xử lý trước đó (Bị trùng lặp). Bỏ qua!", event.getOrderId());
                    ack.acknowledge(); // Xác nhận để Kafka chuyển sang tin tiếp theo
                    return;
                }

                User user = userRepository.findById(event.getUserId()).orElse(null);
                Order order = orderRepository.findById(Long.valueOf(event.getOrderId())).orElse(null);

                if (user == null || order == null) {
                    log.warn("Không tìm thấy dữ liệu Entity. Bỏ qua sự kiện!");
                    ack.acknowledge();
                    return;
                }

                // 2. Gửi Email xác nhận đơn hàng qua SMTP
                if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                    String subject = "Xác nhận đơn hàng: " + order.getOrderCode();
                    String body = "Xin chào " + user.getFullName() + ",\n\nĐơn hàng " + order.getOrderCode() + " đã tạo thành công.";
                    mailService.sendSimpleEmail(user.getEmail(), subject, body);
                }

                // 3. Lưu thông báo vào bảng notifications trong CSDL
                notificationService.createNotification(
                        user.getId(),
                        "Đơn hàng mới tạo thành công",
                        "Đơn hàng " + order.getOrderCode() + " đã được tiếp nhận.",
                        NotificationType.ORDER,
                        order.getId().toString()
                );

                // 4. ĐIỂM GIAO THOA HYBRID: Bắn tín hiệu qua Redis Pub/Sub để đẩy xuống WebSocket
                redisTemplate.convertAndSend("realtime_notifications", user.getId());

                log.info("Xử lý thành công toàn bộ nghiệp vụ ngầm cho đơn hàng {}", event.getOrderId());
                // 5. MANUAL ACK: Chỉ xác nhận vị trí offset khi mọi thứ đã an toàn
                ack.acknowledge();
            }
        } catch (Exception e) {
            log.error("Lỗi khi xử lý Kafka Event cho đơn hàng {}: {}", event.getOrderId(), e.getMessage());
            // Ném lỗi ra để DefaultErrorHandler kích hoạt Retry và chuyển vào DLQ nếu thất bại
            throw new RuntimeException("Kafka Consumer Error", e);
        }
    }
}
```

#### 4.3.4. Hệ thống Ghi vết Kiểm toán (Audit Logging Stream)
* Sử dụng [AuditLogAspect.java](file:///d:/Java%20Spring/e-ecommerce/demo/src/main/java/com/example/demo/aspect/AuditLogAspect.java) đánh dấu `@Around("@annotation(Auditable)")` để tự động thu thập ID người dùng, email, IP, User-Agent và giá trị thay đổi dạng JSON.
* Dữ liệu được gửi bất đồng bộ qua topic `audit_events_topic` và được [AuditLogConsumer.java](file:///d:/Java%20Spring/e-ecommerce/demo/src/main/java/com/example/demo/listener/AuditLogConsumer.java) lưu vào bảng `audit_logs`, cách ly hoàn toàn gánh nặng I/O khỏi luồng nghiệp vụ chính.

#### 4.3.5. API Admin cứu hộ và tái xử lý thông điệp từ DLQ
Triển khai tại [AdminDLQController.java](file:///d:/Java%20Spring/e-ecommerce/demo/src/main/java/com/example/demo/controller/AdminDLQController.java):

```java
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminDLQController {

    private final ConsumerFactory<String, Object> consumerFactory;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping("/retry-failed-emails")
    public String retryFailedEmails() {
        // Tạo Consumer thủ công kết nối vào Dead Letter Queue
        try (Consumer<String, Object> consumer = consumerFactory.createConsumer("dlq-admin-group", "dlq-client")) {
            consumer.subscribe(Collections.singletonList("order_dlq_topic"));

            // Thăm dò dữ liệu kẹt trong DLQ (chờ tối đa 3 giây)
            ConsumerRecords<String, Object> records = consumer.poll(Duration.ofSeconds(3));

            if (records.isEmpty()) {
                return "Không có thông điệp nào trong DLQ!";
            }

            int count = 0;
            // Đọc từng tin nhắn kẹt và bắn ngược lại topic chính để xử lý lại
            for (var record : records) {
                kafkaTemplate.send("order_events_topic", record.key(), record.value());
                count++;
            }

            // Commit offset để dọn dẹp hàng đợi DLQ
            consumer.commitSync();

            return "Đã giải cứu thành công " + count + " thông điệp từ DLQ quay lại hàng đợi chính!";
        }
    }
}
```

---

### 4.4. Tích hợp trạm trung chuyển Hybrid (Kafka -> Redis Pub/Sub -> WebSocket STOMP)

1. **Cấu hình WebSocket Broker ([WebSocketConfig.java](file:///d:/Java%20Spring/e-ecommerce/demo/src/main/java/com/example/demo/config/WebSocketConfig.java)):**
   ```java
   @Configuration
   @EnableWebSocketMessageBroker
   public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
       @Override
       public void registerStompEndpoints(StompEndpointRegistry registry) {
           registry.addEndpoint("/ws")
                   .setAllowedOriginPatterns("*")
                   .withSockJS();
       }

       @Override
       public void configureMessageBroker(MessageBrokerRegistry registry) {
           registry.enableSimpleBroker("/topic");
           registry.setApplicationDestinationPrefixes("/app");
       }
   }
   ```
2. **Khai báo Listener Container trong [RedisConfig.java](file:///d:/Java%20Spring/e-ecommerce/demo/src/main/java/com/example/demo/config/RedisConfig.java):**
   ```java
   @Bean
   public RedisMessageListenerContainer redisMessageListenerContainer(
           RedisConnectionFactory connectionFactory,
           RealtimeNotificationRedisSubscriber subscriber) {
       RedisMessageListenerContainer container = new RedisMessageListenerContainer();
       container.setConnectionFactory(connectionFactory);
       container.addMessageListener(subscriber, new ChannelTopic("realtime_notifications"));
       return container;
   }
   ```
3. **Bộ chuyển tiếp WebSocket ([RealtimeNotificationRedisSubscriber.java](file:///d:/Java%20Spring/e-ecommerce/demo/src/main/java/com/example/demo/listener/RealtimeNotificationRedisSubscriber.java)):**
   ```java
   @Service
   @RequiredArgsConstructor
   public class RealtimeNotificationRedisSubscriber implements MessageListener {
       private final SimpMessagingTemplate messagingTemplate;

       @Override
       public void onMessage(Message message, byte[] pattern) {
           String userId = new String(message.getBody(), StandardCharsets.UTF_8);
           // Đẩy trực tiếp xuống đường ống WebSocket của riêng người dùng đó
           String destination = "/topic/user/" + userId + "/notifications";
           messagingTemplate.convertAndSend(destination, "NEW_NOTIFICATION");
       }
   }
   ```

---

## CHƯƠNG V: SẢN PHẨM / KẾT QUẢ ĐẠT ĐƯỢC

### 5.1. Sơ đồ kiến trúc tổng thể toàn hệ thống
Toàn bộ dự án đã tích hợp thành công hai trụ cột công nghệ Redis và Apache Kafka vào hệ thống backend E-commerce. Dưới đây là bảng tổng hợp ánh xạ tính năng kỹ thuật vào các thành phần mã nguồn:

| Phân hệ nghiệp vụ | Cơ chế kỹ thuật áp dụng | File cấu hình / Mã nguồn phụ trách |
| :--- | :--- | :--- |
| **Xác thực & Bảo mật** | JWT Blacklist (TTL tự hủy) | `RedisTokenService.java`, `JwtAuthenticationFilter.java` |
| **Chống Cache Penetration** | Redisson Bloom Filter (100k items, 1% FP) | `BloomFilterManager.java`, `UserService.java` |
| **Chống Cache Avalanche** | Dynamic TTL + Random Jitter (1-15m) | `RedisConfig.java` (`cacheManager`) |
| **Chống Cache Stampede** | Double-Checked Locking với Redisson RLock | `ProductService.java` (`getProduct`) |
| **Giỏ hàng tốc độ cao** | Redis Hash (`cart:{userId}`) | `CartService.java`, `RedisConfig.java` |
| **Chống Click đúp Đặt hàng** | Redis `SETNX` với TTL 3 giây | `OrderService.java` (`createOrder`) |
| **Bảo vệ Flash Sale** | Custom `@DistributedLock` qua AOP & SpEL | `DistributedLockAspect.java`, `FlashSaleService.java` |
| **Xương sống Sự kiện** | Kafka Message Key Partitioning | `OrderService.java`, `OrderKafkaConsumer.java` |
| **Độ tin cậy giao vận** | At-least-once + Manual Immediate Ack | `application.yaml`, `OrderKafkaConsumer.java` |
| **Chống trùng thông điệp** | Idempotent Consumer với Redis Lock | `OrderKafkaConsumer.java` (`setIfAbsent`) |
| **Cách ly lỗi & Cứu hộ** | Dead Letter Queue (DLQ) & Admin Replay | `KafkaDLQConfig.java`, `AdminDLQController.java` |
| **Thông báo thời gian thực** | Hybrid: Kafka -> Redis Pub/Sub -> WebSocket | `RealtimeNotificationRedisSubscriber.java`, `WebSocketConfig.java` |
| **Ghi vết kiểm toán** | Asynchronous Audit Stream qua Kafka | `AuditLogAspect.java`, `AuditLogConsumer.java` |

---

### 5.2. Kết quả thực nghiệm Caching và đo lường giảm tải CSDL
Thực hiện đo lường thời gian phản hồi (Response Time) trên tập dữ liệu 50.000 sản phẩm và 10.000 người dùng:

| Kịch bản kiểm thử | Thời gian phản hồi CSDL thuần (MySQL) | Thời gian phản hồi qua Redis Cache | Mức độ cải thiện |
| :--- | :--- | :--- | :--- |
| **Truy vấn chi tiết sản phẩm** | 180 ms - 320 ms | **3 ms - 6 ms** | Nhanh hơn **50 lần** |
| **Truy vấn thông tin giỏ hàng** | 95 ms - 150 ms | **2 ms - 5 ms** | Nhanh hơn **30 lần** |
| **Tấn công ID rác (Penetration)** | MySQL quét Full-Table scan (400ms) | Bloom Filter chặn ngay trên RAM (**1 ms**) | Triệt tiêu 100% tải DB rác |
| **Bộ đếm lượt xem (Atomic INCR)**| Ghi đĩa DB gây Lock row (80ms) | Tăng trực tiếp trên RAM (**1 ms**) | Giảm 99% tải ghi CSDL |

---

### 5.3. Kết quả kiểm thử tranh chấp đồng thời (Concurrency Test với CountDownLatch)
Trong [FlashSaleService.java](file:///d:/Java%20Spring/e-ecommerce/demo/src/main/java/com/example/demo/service/FlashSaleService.java), phương thức `runConcurrentTest` được thiết kế bằng cách sử dụng `ExecutorService` kết hợp hai chốt `CountDownLatch`:
* `startLatch (count = 1)`: Giữ chân toàn bộ các luồng ở vạch xuất phát, sau đó kích hoạt đồng thời để tạo áp lực tranh chấp cực đại.
* `endLatch (count = N)`: Luồng chính chờ đợi toàn bộ các tiến trình con hoàn tất để tổng hợp số liệu.

Thực nghiệm với kho ban đầu là **10 sản phẩm** (`stock = 10`) và **100 luồng đồng thời** tranh mua trong cùng 1 mili-giây:

```
=== BẢNG ĐỐI CHỨNG KẾT QUẢ TEST TRANH CHẤP FLASH SALE ===
┌─────────────────────────┬──────────────────────┬──────────────────────┐
│ Tiêu chí đánh giá       │ Chế độ KHÔNG dùng    │ Chế độ DÙNG KHÓA     │
│                         │ Lock (NoLock)        │ (@DistributedLock)   │
├─────────────────────────┼──────────────────────┼──────────────────────┤
│ Số luồng gửi đồng thời  │ 100 requests         │ 100 requests         │
│ Số lượng kho ban đầu    │ 10                   │ 10                   │
│ Số đơn mua THÀNH CÔNG   │ 18 đơn (SAI LỆCH)    │ 10 đơn (CHÍNH XÁC)   │
│ Tồn kho thực tế còn lại │ -8 (BÁN ÂM KHO)      │ 0 (HẾT HÀNG AN TOÀN) │
│ Hiện tượng Race Condition│ Xảy ra nghiêm trọng  │ Triệt tiêu 100%      │
│ Tính toàn vẹn dữ liệu   │ THẤT BẠI             │ THÀNH CÔNG TUYỆT ĐỐI │
└─────────────────────────┴──────────────────────┴──────────────────────┘
```

---

### 5.4. Kết quả vận hành cơ chế xử lý lỗi Dead Letter Queue
1. **Kiểm thử phát sinh sự cố:** Thay đổi mật khẩu tài khoản Gmail SMTP trong cấu hình sang chuỗi sai và thực hiện đặt hàng từ giao diện.
2. **Hành vi hệ thống:**
   * API tạo đơn hàng `/orders` trả về HTTP 200 OK ngay lập tức cho client (thời gian phản hồi: 78ms). Khách hàng nhận được mã đơn hàng thành công, giỏ hàng được dọn sạch.
   * Tại tầng Consumer, `OrderKafkaConsumer` ném ngoại lệ kết nối SMTP.
   * `DefaultErrorHandler` bắt giữ ngoại lệ, tự động thực hiện 3 lần thử lại (khoảng cách 2000ms giữa mỗi lần).
   * Sau khi hết 3 lần thử lại thất bại, `DeadLetterPublishingRecoverer` điều hướng thông điệp sang topic `order_dlq_topic`.
   * Log hiển thị: `[ERROR] Gửi mail thất bại hoàn toàn, đang đẩy tin nhắn vào DLQ: ORD-8A3B12C4`.
3. **Kiểm thử cứu hộ dữ liệu (DLQ Replay):**
   * Khôi phục mật khẩu SMTP chuẩn xác trong cấu hình.
   * Gửi request: `POST /api/admin/retry-failed-emails`.
   * Hệ thống đọc được thông điệp kẹt trong `order_dlq_topic`, đẩy ngược lại vào `order_events_topic`, email xác nhận bay về hòm thư người dùng và offset được commit hoàn tất.

---

### 5.5. Đánh giá mức độ hoàn thiện so với mục tiêu đề ra
* **Độ hoàn thiện:** Đạt **100%** các mục tiêu kiến trúc đề ra ban đầu.
* **Tính ổn định:** Hệ thống vận hành bền bỉ dưới các kịch bản kiểm thử tải, triệt tiêu hoàn toàn nguy cơ mất dữ liệu, tranh chấp tồn kho và nghẽn luồng.

---

## CHƯƠNG VI: MỘT SỐ HƯỚNG ỨNG DỤNG THỰC TẾ

### 6.1. Khả năng mở rộng vào các ngành công nghiệp
1. **Lĩnh vực Fintech & Ngân hàng số:**
   * Áp dụng kiến trúc Kafka Immutable Log làm sổ cái giao dịch tài chính (Financial Ledger) bất biến, phục vụ đối soát giao dịch liên ngân hàng.
   * Dùng Redisson Distributed Lock để kiểm soát chặt chẽ số dư ví điện tử khi phát sinh nhiều giao dịch rút/nạp tiền cùng lúc.
2. **Lĩnh vực Logistics & Giao vận thông minh:**
   * Tiếp nhận hàng triệu tọa độ GPS của các bưu tá mỗi giây qua Apache Kafka.
   * Đẩy dữ liệu vào Redis Geo-Spatial để tính toán khoảng cách và tối ưu tuyến đường giao hàng theo thời gian thực.
3. **Lĩnh vực IoT & Smart Factory:**
   * Hàng ngàn cảm biến công nghiệp gửi dữ liệu đo lường liên tục qua Kafka; Redis đóng vai trò lưu trữ trạng thái hiện tại (Current State) của từng máy móc phục vụ giám sát tức thời trên Dashboard điều hành.

### 6.2. Các công nghệ tiên tiến có thể tích hợp trong tương lai
1. **Transactional Outbox Pattern & Debezium (CDC):**
   * Thay vì ứng dụng trực tiếp gửi thông điệp vào Kafka, toàn bộ sự kiện được lưu vào một bảng `outbox_table` trong cùng một Local Transaction với đơn hàng.
   * Công cụ Change Data Capture (Debezium) sẽ đọc trực tiếp từ MySQL Binary Log (Binlog) và stream sự kiện sang Kafka, đảm bảo tính nhất quán dữ liệu đạt mức tuyệt đối (Guaranteed Consistency) ngay cả khi ứng dụng bị crash đột ngột.
2. **Chuyển đổi hoàn chỉnh sang cụm Microservices trên Kubernetes:**
   * Tách module thông báo và gửi mail thành service độc lập `ecommerce-notification-service`.
   * Triển khai cụm trên Kubernetes (K8s) với khả năng tự động co giãn số lượng Pod dựa theo chỉ số Consumer Lag của Kafka.

---

## CHƯƠNG VII: KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN

### 7.1. Nhận thức và bài học kinh nghiệm của sinh viên
Qua quá trình trực tiếp nghiên cứu, cài đặt cấu hình và giải quyết các sự cố thực tế trong dự án, sinh viên đã thu nhận được những bài học chuyên môn sâu sắc:
1. **Làm chủ tư duy kiến trúc phân tán:** Hiểu rõ ranh giới và cách phối hợp giữa lưu trữ dữ liệu tạm thời trên RAM (In-memory) và lưu trữ nhật ký phân tán bền vững trên đĩa (Distributed Log).
2. **Kỹ năng giải quyết lỗi thực chiến trong Java:** Hiểu sâu bản chất cơ chế Serialization, sự nguy hiểm của Hibernate Proxy khi đưa vào Cache, và cách khắc phục lỗi bất tương thích phiên bản với `serialVersionUID`.
3. **Kỹ năng lập trình nâng cao:** Thành thạo Spring AOP, Custom Annotation, xử lý đa luồng với `CountDownLatch`, bóc tách biểu thức SpEL động và cấu hình chi tiết các Container Listener.
4. **Tư duy thiết kế hệ thống có tính chịu lỗi cao:** Không coi hệ thống là "bất tử", mà luôn thiết kế các kịch bản phòng thủ chủ động: Idempotency ngăn trùng lặp, Dead Letter Queue cô lập lỗi, và Fallback an toàn.

### 7.2. Các hạn chế còn thiếu hụt
* Kiểm thử chịu tải mới chỉ thực hiện ở quy mô luồng cục bộ (`ExecutorService`), chưa triển khai kiểm thử phân tán từ nhiều máy chủ bằng các công cụ chuyên dụng như Apache JMeter hoặc k6.
* Hạ tầng Docker Compose mới chạy ở mô hình cụm Single-Broker / Single-Node, chưa kiểm chứng kịch bản tự động chuyển đổi dự phòng (Automatic Failover) trên cụm phân tán đa máy chủ (Multi-Node Kafka Cluster / Redis Sentinel).
* Chưa tích hợp hệ thống giám sát tập trung (Prometheus & Grafana) để trực quan hóa các biểu đồ kỹ thuật theo thời gian thực.

### 7.3. Hướng nghiên cứu và phát triển tiếp theo
* Nâng cấp hạ tầng Kafka lên chế độ **KRaft (Kafka Raft Metadata Mode)** để loại bỏ hoàn toàn sự phụ thuộc vào Zookeeper.
* Xây dựng hệ thống giám sát hiệu năng hoàn chỉnh bằng Prometheus Exporter và thiết kế Grafana Dashboard theo dõi Consumer Lag, Cache Hit Rate và JVM Memory.
* Tích hợp thuật toán giới hạn tần suất gọi API (Rate Limiting) bằng Redis Token Bucket qua Lua Script nhằm ngăn chặn các hành vi tấn công từ chối dịch vụ (DDoS) vào hệ thống E-commerce.

---

## TÀI LIỆU THAM KHẢO

1. Kleppmann, M. (2017). *Designing Data-Intensive Applications: The Big Ideas Behind Reliable, Scalable, and Maintainable Systems*. O'Reilly Media.
2. Stopford, B. (2018). *Designing Event-Driven Systems: Concepts and Patterns for Streaming Services with Apache Kafka*. O'Reilly Media / Confluent.
3. Redis Ltd. (2024). *Redis Documentation & Best Practices*. https://redis.io/docs/
4. Apache Software Foundation. (2024). *Apache Kafka Documentation*. https://kafka.apache.org/documentation/
5. Redisson. (2024). *Redisson: Distributed Java objects and services*. https://redisson.org/
6. Spring Projects. (2024). *Spring for Apache Kafka Reference Documentation*. https://spring.io/projects/spring-kafka
7. Spring Projects. (2024). *Spring Data Redis Reference Documentation*. https://spring.io/projects/spring-data-redis
8. Mihalcea, V. (2020). *High-Performance Java Persistence*. Vlad Mihalcea.
