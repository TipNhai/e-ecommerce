# Kiến trúc dự án E-Commerce & Bài học kinh nghiệm (AI Knowledge Base)

> [!IMPORTANT]
> Tài liệu này đóng vai trò là "bộ não" tổng hợp kiến trúc hệ thống và những bài học/lỗi đã giải quyết. 
> **Quy tắc bắt buộc:** Mỗi khi cấu trúc dự án (Database, API, Security, Gateway, ...) có thay đổi mới, các Agents/AI **BẮT BUỘC** phải tự động cập nhật lại file này để duy trì tính chính xác của Knowledge Base cho các phiên làm việc sau!

---

## 1. Tổng quan dự án (Project Overview)
Dự án là một hệ thống **E-Commerce (Thương mại điện tử)** được xây dựng theo kiến trúc **Monolithic** (Nguyên khối).
- **Backend Framework:** Java Spring Boot 3.
- **Data Access:** Spring Data JPA, Hibernate.
- **Database:** MySQL.
- **Cache / In-memory Data:** Redis (Quản lý Token, Giỏ hàng hoặc Data đệm).
- **Security:** Spring Security + JWT Authentication.
- **Payment Gateways:** Tích hợp đa cổng (VNPAY, MoMo) qua API.

---

## 2. Kiến trúc Tầng (Layered Architecture)
Dự án tuân thủ chặt chẽ mô hình **MVC** và **Dependency Injection**:
- **Controllers (`controller/`):** Xử lý HTTP Request/Response. Sử dụng `@RestController`.
- **Services (`service/`):** Chứa Business Logic. Áp dụng `@Transactional` cho các thao tác DB.
- **Repositories (`repository/`):** Mở rộng `JpaRepository`. Có sử dụng `@EntityGraph` để giải quyết bài toán truy vấn N+1 (Ví dụ: `OrderRepository`).
- **Entities (`entity/`):** Model ánh xạ 1-1 với các bảng trong MySQL.
- **DTOs (`dto/`):** Các Data Transfer Objects (`Request` và `Response`) để giao tiếp với Client, bọc trong `ApiResponse<T>`.
- **Mappers (`mapper/`):** (Có thể dùng MapStruct hoặc thủ công) chuyển đổi giữa Entity và DTO.
- **Exceptions (`exception/`):** Xử lý lỗi tập trung bằng `@RestControllerAdvice` (`GlobalExceptionHandler`), chuẩn hóa lỗi qua `AppException` và `ErrorCode`.

> [!TIP]
> **Code Convention:** Dự án sử dụng mạnh mẽ Lombok để tiết kiệm boilerplate code. Các class Service/Controller thường dùng `@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)` kết hợp `@RequiredArgsConstructor` để tự động Inject dependencies (Constructor Injection) mà không cần `@Autowired`.

---

## 3. Mô hình Dữ liệu (Data Model - Entities)
Các thực thể chính và mối quan hệ:
- **User:** Trái tim của hệ thống. 1 User có nhiều `Address` (Địa chỉ giao hàng), có nhiều `Role` (Quyền), và nhiều `Order` (Đơn hàng).
- **Product:** Thuộc về `Category` (Danh mục) và `Brand` (Thương hiệu). 1 Product có nhiều `ProductImage` (Ảnh) và `Review` (Đánh giá).
- **Order & OrderItem:** Đơn hàng và chi tiết đơn hàng. `Order` lưu thông tin vận chuyển, tổng tiền, trạng thái (`OrderStatus`).
- **Payment:** Gắn với `Order`. Lưu trữ trạng thái giao dịch, phương thức thanh toán (`PaymentMethod`) và Transaction ID từ đối tác.
- **Cart & CartItem:** Quản lý giỏ hàng của người dùng.

---

## 4. Kiến trúc Security & Authentication
Hệ thống sử dụng **Stateless Authentication** với JWT Token và Redis.
- **Chuyển đổi Subject:** Dự án lưu `subject` trong payload JWT là `UUID` của người dùng (thay vì `username`).
- **Entity `User`:** Đã implements `UserDetails` của Spring Security để tương thích hoàn toàn với bộ lọc chuẩn.
- **Custom Filter (`JwtAuthenticationFilter`):** 
  - Trích xuất Bearer Token.
  - Lấy UUID từ `subject`.
  - Dùng `UserDetailsServiceImpl` gọi `userRepository.findById(uuid)` để lấy `User`.
  - Lưu đối tượng `User` trực tiếp vào `SecurityContext` làm **Principal**.
- **Quản lý Token với Redis (`RedisTokenService`):** Redis được dùng để lưu trữ Refresh Token (và có thể là access token để chặn - blacklist) với TTL (Time-To-Live) tự động hết hạn, giúp tăng hiệu năng kiểm tra quyền.

> [!IMPORTANT]
> **Best Practice Lấy User ID ở API:**
> - **TUYỆT ĐỐI KHÔNG DÙNG:** `auth.getName()` trong Service để lấy ID. Hàm này sẽ trỏ tới `getUsername()` và trả về tên đăng nhập (ví dụ "admin"), làm hỏng các logic tìm kiếm bằng UUID!
> - **CHUẨN CLEAN CODE:** Nhận thẳng đối tượng User ở tham số Controller thông qua `@AuthenticationPrincipal User user`, sau đó lấy `user.getId()` truyền xuống Service.

---

## 5. Kiến trúc Payment (Thanh toán)
Module thanh toán tuân theo mẫu thiết kế **Strategy / Adapter Pattern** rất dễ mở rộng:
- **Factory (`PaymentGatewayFactory`):** Tự động lựa chọn cổng thanh toán dựa theo Enum `PaymentMethod` (MOMO, VNPAY, COD).
- **Adapters (`MomoPaymentAdapter`, `VnpayPaymentAdapter`):** Implement chung một Interface `PaymentGatewayAdapter`. 
  - `initiatePayment()`: Mã hóa chữ ký HMAC-SHA256, gọi HTTP POST sang Gateway qua `RestClient` hoặc render URL redirect.
  - `processCallback()`: Xử lý Return URL và IPN. Kiểm tra Checksum (chữ ký số) để chống giả mạo, cập nhật `PaymentStatus`.
- **Cấu trúc URL bảo mật:**
  - `/initiate` (Yêu cầu đăng nhập).
  - `/callback/{method}` (Public cho Web/App redirect về).
  - `/ipn/{method}` (Public, webhook Server-to-Server dành riêng cho MoMo/VNPAY gọi ngầm).

---

## 6. Các bài học và lỗi (Bugs) phổ biến đã khắc phục
1. **Lỗi ngầm "ClassCastException" trong JwtFilter:** 
   - **Tình trạng:** Quăng lỗi ngầm khiến SecurityContext bị xóa, API trả về 403 `You do not have permission`.
   - **Cách giải quyết:** Đã thêm `implements UserDetails` vào `User.java`.
2. **Lỗi Order Not Found khi khởi tạo thanh toán:**
   - **Tình trạng:** JPA tìm Order với id = 8 và `userId` = "admin" (do lấy từ `auth.getName()`), kết quả là Null.
   - **Cách giải quyết:** Refactor `PaymentController` dùng `@AuthenticationPrincipal User user` để lấy chính xác UUID.
3. **Lỗi MoMo "URI with undefined scheme":**
   - **Tình trạng:** Bị null config endpoint do file `application.yaml` lùi lề sai (nested cấu hình `momo` vào trong `vnpay`).
   - **Cách giải quyết:** Chỉnh YAML để `vnpay` và `momo` nằm ngang hàng dưới `payment:`.
4. **Lỗi MoMo 11007 - Chữ ký không hợp lệ:**
   - **Tình trạng:** Hàm tạo chữ ký chạy đúng, String tạo ra đúng tài liệu, nhưng MoMo báo sai. Nguyên nhân do cấu hình nhầm chuỗi public test `secret-key`.
   - **Cách giải quyết:** Sửa `secret-key` trong YAML thành khóa test public chuẩn của MoMo (`K951B6PE1waDMi640xX08PD3vg6EkVlz`).
5. **Kẹt Token cũ ở Redis (Lỗi Logic Subject):**
   - **Tình trạng:** Đã sửa logic tạo JWT từ `username` sang `UUID` nhưng hàm `/refresh-token` vẫn nhả ra JWT mang payload `username`.
   - **Cách giải quyết:** Mỗi khi thay đổi cấu trúc dữ liệu nhạy cảm trong hệ thống Cache/Token, phải dọn dẹp bằng cách flush sạch Redis (`FLUSHALL`).
6. **Lỗi "Unexpected number of Base64URL parts, must be three" khi parse Token:**
   - **Tình trạng:** Ứng dụng đang chạy ổn, sau đó đột nhiên tất cả token đều bị lỗi này dù vừa được cấp mới. Token không bị truncate hay edit.
   - **Nguyên nhân gốc rễ:** Trong `JwtTokenProvider.java`, Payload JWT được tạo sai cách: `new Payload(claimsSet.toString())`. Hàm `toString()` của `JWTClaimsSet` không đảm bảo trả về JSON chuẩn, khiến Nimbus-JOSE đóng gói nội dung dưới dạng binary thay vì JSON, làm cấu trúc 3 phần `header.payload.signature` bị phá vỡ khi parse lại.
   - **Cách giải quyết:** Thay `JWSObject` + `Payload` bằng cách dùng thẳng `new SignedJWT(header, claimsSet)`, rồi gọi `.sign()` trực tiếp trên đó. Đây là API đúng của thư viện Nimbus-JOSE cho JWT.
   - **File sửa:** `config/JwtTokenProvider.java`, hàm `buildToken()`.
