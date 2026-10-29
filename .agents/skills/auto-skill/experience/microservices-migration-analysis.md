# Đánh giá Khả thi Nâng cấp lên Microservices cho dự án E-commerce

## Bối cảnh
Dự án hiện tại là một monolithic application được viết bằng Java Spring Boot, sử dụng JPA cho các liên kết dữ liệu trực tiếp và dùng một database chung cho tất cả các domain (User, Order, Product, Cart, v.v.).

## Phát hiện cốt lõi
1. **Coupling cao giữa các service:** Các service như `OrderService` có sự phụ thuộc lớn vào nhiều Repositories khác nhau (`CartRepository`, `AddressRepository`, `Product`) và thực hiện thao tác trực tiếp trên các entity thuộc domain khác (ví dụ: `product.setQuantity()`).
2. **Transaction nguyên khối:** Phương thức `createOrder` sử dụng một `@Transactional` duy nhất để cập nhật giỏ hàng, trừ tồn kho sản phẩm, áp dụng mã giảm giá và tạo đơn hàng.
3. **Công nghệ hỗ trợ:** Dự án đã có sẵn Kafka, Redis, MapStruct, khá thuận lợi cho quá trình chuyển đổi.

## Bài học / Hướng giải quyết (Roadmap)
Để nâng cấp lên Microservices, cần thực hiện các bước sau:
1. **Tách Database:** Tách database chung thành các database độc lập cho từng service (Ví dụ: `order_db`, `product_db`, `user_db`).
2. **Thay thế Foreign Keys:** Loại bỏ các quan hệ `@ManyToOne`, `@OneToMany` xuyên domain. Thay vì lưu object, chỉ lưu ID (ví dụ: trong Order chỉ lưu `userId`, không lưu object `User`).
3. **Giao tiếp liên dịch vụ (Inter-Service Communication):** Thay thế việc gọi trực tiếp Repository của domain khác bằng OpenFeign (REST API) hoặc Event-driven architecture (sử dụng Kafka đã có sẵn trong `pom.xml`).
4. **Xử lý Distributed Transaction:** Áp dụng **Saga Pattern** cho quy trình đặt hàng.
5. **Gateway & Auth:** Thiết lập API Gateway và trích xuất phần Authentication thành một Identity Service độc lập.
