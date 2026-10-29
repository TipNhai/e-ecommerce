# Sổ Tay Kinh Nghiệm & Khắc Phục Lỗi Redis + Spring Boot

Tài liệu này ghi nhận lại toàn bộ hành trình xử lý sự cố liên quan đến Redis Caching, Hibernate Proxies, và Jackson Serialization trong quá trình xây dựng dự án. Đây là những lỗi vô cùng kinh điển mà gần như lập trình viên Java nào cũng sẽ gặp phải khi bắt đầu áp dụng Redis vào dự án thực tế.

---

## 1. Vấn Đề Thực Sự Của Project Là Gì?

Khi chúng ta bắt đầu, dự án trông có vẻ đang gặp một lỗi đơn thuần là "Không thể serialize JSON". Nhưng thực tế, nó là một chuỗi domino của **3 sai lầm cốt lõi đan xen vào nhau**:

1. **Lỗi Hạ Tầng (Infrastructure):** Docker container của Redis được bật, nhưng lại **quên mở port** (không ánh xạ `-p 6379:6379`). Dẫn tới hệ điều hành từ chối kết nối (`Connection refused`).
2. **Lỗi Bọc Ngoại Lệ (Swallowed Exception):** Do file `JwtAuthenticationFilter` sử dụng `try-catch` và giấu đi lỗi Redis, các API public vẫn hoạt động bình thường, tạo ra "ảo giác" rằng Redis vẫn đang sống nhăn răng. Điều này khiến bạn mất rất nhiều thời gian dò dẫm sai hướng.
3. **Lỗi Thiết Kế Caching (Architecture):** Sử dụng trực tiếp Jackson JSON (`GenericJackson2JsonRedisSerializer`) để lưu trữ (cache) trực tiếp các đối tượng **Hibernate Entity (`User`, `Cart`)** thay vì các đối tượng **DTO (`UserResponse`)**.

---

## 2. Các Lỗi Sai Cụ Thể Và Bài Học Rút Ra

### Sai Lầm 1: Cache Trực Tiếp Hibernate Entity Bằng Jackson (JSON)
- **Hậu quả:** Khi Jackson cố gắng chuyển đổi Entity `User` thành JSON, nó đã quét trúng thuộc tính `addresses` (đang được Hibernate quản lý dưới dạng Lazy Load bằng class proxy `PersistentBag`). Jackson bèn lưu luôn cả tên class `PersistentBag` này vào Redis. Khi lấy data từ Redis ra, Jackson tạo lại một đối tượng `PersistentBag` "mồ côi" không dính dáng gì đến Database, dẫn đến lỗi kinh điển: `Cannot lazily initialize collection (no session)`.
- **Kinh nghiệm:** 
  - **NGUYÊN TẮC VÀNG:** Hạn chế tối đa việc cache các Entity JPA trực tiếp vào Redis nếu dùng chuẩn JSON. Hãy luôn map chúng sang DTO (như `CartResponse`, `UserResponse`) rồi mới đem DTO đó đi cache. DTO là một object sạch, không chứa Proxy, không Lazy Load, không vòng lặp.
  - **Đọc thêm uy tín:** [Baeldung: Spring Data Redis & Jackson](https://www.baeldung.com/spring-data-redis-properties) | [Hibernate LazyInitializationException](https://vladmihalcea.com/the-best-way-to-handle-the-lazyinitializationexception/)

### Sai Lầm 2: Cố gắng vá lỗi bằng `@JsonIgnoreProperties` và Thư viện quá mới
- **Hậu quả:** Ban đầu ta cố sửa lỗi Jackson bằng cách thêm hàng loạt annotation như `@JsonIgnore`, `@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})` và thậm chí cài thêm module `jackson-datatype-hibernate6`. Tuy nhiên, vì Spring Boot phiên bản mới sử dụng Hibernate 7, module Hibernate 6 kia đã xung đột và làm sập luôn toàn bộ dự án ở ngay lúc khởi động (`Error creating bean entityManagerFactory`).
- **Kinh nghiệm:** 
  - "Đừng ép mỡ, ép dầu". Khi một thư viện sinh ra không phải để giải quyết bài toán đó (Jackson không giỏi trong việc handle Hibernate proxies), đừng cố nhét thêm hàng chục config vào nó. Hãy tìm một giải pháp Native hơn.

### Sai Lầm 3: Quên `implements Serializable` (Và bẫy Nested Object)
- **Hậu quả:** Khi chúng ta quay xe về giải pháp đúng đắn là dùng cơ chế Serialize nhị phân mặc định của Java (`JdkSerializationRedisSerializer`), nó báo lỗi `Cannot serialize value...` kèm `NotSerializableException`.
- **Kinh nghiệm 1 (Cơ bản):** Mọi class mà bạn có ý định truyền tải qua mạng, lưu vào ổ cứng, hoặc tống vào Redis thông qua Java Serialize thì đều **bắt buộc** phải có đuôi `implements Serializable`. (Vd: `public class CartResponse implements Serializable`).
- **Kinh nghiệm 2 (Bẫy Nested Object):** Cơ chế Serialization có tính đệ quy. Nếu class `FavoriteResponse` đã implements Serializable, nhưng bên trong nó chứa một list các object khác (vd: `List<ProductSummaryResponse> products`), thì **TẤT CẢ** các object con này cũng bắt buộc phải implements Serializable. Nếu sót một class con nào, quá trình sẽ ném lỗi ngay lập tức. Khuyên dùng: Thêm `Serializable` đồng loạt cho toàn bộ các DTO/Response.
- **Đọc thêm uy tín:** [Baeldung: Introduction to Java Serialization](https://www.baeldung.com/java-serialization)

### Sai Lầm 4: Bỏ quên `serialVersionUID` gây lỗi InvalidClassException
- **Hậu quả:** Khi gọi API đọc data từ Redis, app crash với lỗi `Failed to deserialize payload` kèm `java.io.InvalidClassException: local class incompatible`.
- **Nguyên nhân:** Dữ liệu cũ trong Redis được lưu với một mã phiên bản class ngẫu nhiên do Java tự sinh ra (vd: `-6663755581793391186`). Khi bạn thay đổi code hoặc cấu trúc class thay đổi, mã này bị lệch. Khi đọc data cũ lên, hai mã phiên bản không khớp nhau nên Java từ chối deserialize để bảo vệ an toàn.
- **Kinh nghiệm:** Khi một class implements `Serializable`, **luôn luôn** khai báo tường minh phiên bản của nó để khóa lại:
  ```java
  import java.io.Serial;
  import java.io.Serializable;

  public class YourResponse implements Serializable {
      @Serial
      private static final long serialVersionUID = 1L;
      // ...
  }
  ```
  Nếu đã lỡ dính lỗi này do dữ liệu cũ nằm trong Redis, bạn buộc phải xóa cache rác để hệ thống lưu lại từ đầu. Với dự án chạy Redis bằng Docker, lệnh xóa nhanh là: `docker exec -it redis-server redis-cli flushall`.

---

## 3. Cách Sử Dụng Redis Hiệu Quả Nhất Cho Dự Án Này

Thông qua quá trình sửa lỗi, đây là cấu hình hoàn hảo và ổn định nhất mà dự án đang dùng:

```java
@Bean
public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(60))
            // Key thì lưu dạng String cho dễ đọc trong Redis Desktop Manager
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            // Value thì lưu bằng Native Java Serialization để dẹp tan mọi thể loại lỗi Proxy, Vòng lặp, Java 8 Time.
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new JdkSerializationRedisSerializer()));

    return RedisCacheManager.builder(connectionFactory).cacheDefaults(config).build();
}
```

---

## 4. Cơ Chế Tương Tác Của Redis Với Các File Trong Dự Án

Để Redis có thể hoạt động trơn tru trong một dự án Spring Boot, nó cần sự phối hợp nhịp nhàng của nhiều thành phần. Dưới đây là danh sách các file đóng vai trò quan trọng:

### 1. `RedisConfig.java` (Trái Tim Của Redis)
- **Nhiệm vụ:** Đây là nơi thiết lập kết nối và quy định "cách thức" Redis đọc/ghi dữ liệu.
- Trong dự án này, nó quyết định thời gian sống của Cache (`entryTtl`) và quan trọng nhất là chỉ định **Serializer** (`JdkSerializationRedisSerializer`) để quyết định xem object sẽ biến thành chuỗi JSON hay mã nhị phân khi lưu xuống RAM.

### 2. Các file `*Service.java` (`CartService`, `UserService`, ...)
- **Nhiệm vụ:** Đóng vai trò là "Cửa khẩu". Chúng chứa các Annotation Caching để ra lệnh cho Redis.
- `@Cacheable`: "Nếu có dữ liệu trong Redis rồi thì lấy ra dùng, đừng gọi Database nữa. Nếu chưa có thì gọi Database rồi lưu vào Redis".
- `@CacheEvict`: "Xóa dữ liệu cũ trong Redis đi để lần sau hệ thống bắt buộc phải lấy data mới từ Database" (thường dùng khi Update hoặc Delete).

### 3. Tại sao từng có `@Override <S extends User> S save(S entity);` ở tầng Repository?
- **Sự thật thú vị:** Hàm `save()` là hàm có sẵn của Spring Data JPA. Bình thường bạn không cần phải viết nó ra.
- **Lý do phải Override:** Nếu bạn thiết kế Caching ở tầng Repository (Cách cũ trước khi refactor), bạn bắt buộc phải ghi đè (`@Override`) lại hàm này chỉ để **có chỗ dán cái mác `@CacheEvict` lên trên nó**. Nhờ đó, mỗi khi bạn lưu hoặc cập nhật (`save`) một Entity, Spring sẽ tự động hiểu là phải đi xóa Cache cũ.
- Hiện tại, vì chúng ta đã chuyển toàn bộ Caching lên tầng `Service` (chuẩn Sách giáo khoa), hàm `save()` đã được gọi bên trong các hàm Service (nơi đã có sẵn `@CacheEvict`), nên ta không cần thủ thuật `@Override` này ở Repository nữa.

### 4. Các file DTO (`UserResponse.java`, `CartResponse.java`, ...)
- **Nhiệm vụ:** Đây chính là những "kiện hàng" thực sự được đóng gói và gửi đi cho Redis giữ dùm. 
- Mối liên hệ: Bắt buộc phải có `implements Serializable` thì Redis (thông qua JdkSerializationRedisSerializer) mới chịu nhận hàng.

---

### Tips Tra Cứu Và Học Hỏi Thêm
Nếu gặp lại bất kỳ vấn đề gì về Spring Boot và Redis, bạn hãy tra cứu theo các từ khóa tiếng Anh sau trên Google/StackOverflow:
1. *Lỗi mất session khi đọc từ Cache:* `"Cannot lazily initialize collection (no session) Jackson Redis Spring Boot"`
2. *Lỗi vòng lặp vô hạn:* `"StackOverflowError circular reference Jackson Spring Data JPA"`
3. *Lỗi cấu hình Caching cơ bản:* `"Spring Boot @Cacheable best practices Baeldung"`

Hãy giữ file này như một nhật ký chiến đấu. Những khó khăn hôm nay chắc chắn sẽ là bệ phóng cho kinh nghiệm kiến trúc hệ thống của bạn ngày mai!
