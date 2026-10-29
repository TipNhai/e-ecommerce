# Hướng Dẫn Tự Thực Hành: Nâng Cấp Redis Caching (Giai đoạn 2)

Tuyệt vời! Việc tự tay cài đặt các pattern này là cách tốt nhất để bạn thực sự hiểu rõ cách Redis vận hành trong môi trường Production. Dưới đây là lộ trình để bạn **tự mình thực hiện**, kèm theo các ví dụ minh họa và giải thích chi tiết.

---

## Phần 1: Xử lý Cache Penetration & Cache Breakdown bằng Spring Cache Annotation

### Mục tiêu
- **Chống Penetration:** Cho phép cache lại những giá trị "rỗng" hoặc "null" để chặn đứng việc attacker liên tục query một ID không tồn tại xuống Database.
- **Chống Breakdown:** Khóa (Lock) lại luồng xử lý, đảm bảo khi một hot key bị miss khỏi cache, chỉ có đúng 1 request được phép đi chọc vào Database, các request khác phải đứng đợi lấy kết quả từ cache sau khi request đầu tiên thành công.

### Cách bạn tự thực hiện
Bạn hãy vào các class Service đang có `@Cacheable` (ví dụ `UserService.java`, `CartService.java`, v.v.).

**1. Sửa đổi cú pháp Annotation:**
Xóa thuộc tính `unless = "#result == null"` và thêm `sync = true`.
- Thuộc tính `sync = true` yêu cầu Spring Cache sử dụng local lock (cơ chế khóa cục bộ của JVM) cho key tương ứng trong lúc query dữ liệu.

**Ví dụ tham khảo (Đừng copy y hệt, hãy tự gõ lại để quen tay nhé):**
```java
// CODE CŨ TRONG DỰ ÁN CỦA BẠN:
// @Cacheable(value = "users", key = "#id", unless = "#result == null")
// public UserResponse getUserById(String id) { ... }

// CODE MỚI BẠN SẼ SỬA THÀNH:
@Cacheable(value = "users", key = "#id", sync = true)
public UserResponse getUserById(String id) {
    // Logic bên trong giữ nguyên
    return userMapper.toResponse(userRepository.findById(id)
            .orElseThrow(()->new AppException(USER_NOT_FOUND)));
}
```

*Lưu ý nhỏ cho bạn:* Đối với `getUserById`, vì trong hàm bạn đang dùng `orElseThrow()`, Spring sẽ không bao giờ cache được giá trị rỗng (vì nó ném lỗi trước khi return). Để giải quyết triệt để Penetration cho user bằng Spring Cache thuần, bạn sẽ phải sửa logic hàm này để nó trả về một object `UserResponse` đánh dấu là rỗng thay vì throw exception.

Tuy nhiên, có một cách giải quyết **triệt để và chuyên nghiệp hơn rất nhiều** mà không cần phải lưu rác (cache rỗng) vào Redis. Đó là sử dụng **Bloom Filter**.

---

## Phần 1.5: (Nâng cao) Xử lý triệt để Cache Penetration bằng Bloom Filter

### Mục tiêu
Thay vì phải cache hàng ngàn giá trị `null` gây tốn bộ nhớ, chúng ta sử dụng **Bloom Filter**. Đây là một cấu trúc dữ liệu cực kỳ tiết kiệm bộ nhớ, cho phép kiểm tra siêu tốc xem "Một ID CÓ THỂ tồn tại trong Database hay CHẮC CHẮN KHÔNG tồn tại".
- Nếu Bloom Filter nói: "Không tồn tại" -> Chặn request ngay lập tức, trả về lỗi (Không gọi DB, không gọi Redis Cache).
- Nếu Bloom Filter nói: "Có thể tồn tại" -> Cho phép đi tiếp vào hàm để xử lý.

### Cách bạn tự thực hiện
Bạn sẽ cần dùng thư viện **Redisson** (thư viện Redis client nâng cao cho Java).

**1. Thêm dependency vào `pom.xml`:**
```xml
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>3.27.0</version> <!-- Bạn có thể check version mới nhất -->
</dependency>
```

**2. Khởi tạo Bloom Filter:**
Tạo một service để setup Bloom Filter. Bạn cần load toàn bộ ID hiện có trong Database vào Bloom Filter khi ứng dụng khởi động.

**Ví dụ tham khảo (Tự gõ lại nhé):**
```java
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;

@Service
public class BloomFilterManager {

    private final RedissonClient redissonClient;
    private final UserRepository userRepository; // Giả sử chống penetration cho User
    private RBloomFilter<String> userBloomFilter;

    public BloomFilterManager(RedissonClient redissonClient, UserRepository userRepository) {
        this.redissonClient = redissonClient;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        // Tạo Bloom Filter tên là "userBloomFilter" trên Redis
        userBloomFilter = redissonClient.getBloomFilter("userBloomFilter");
        
        // Khởi tạo: dự kiến chứa 100.000 phần tử, tỷ lệ sai số (false positive) là 1% (0.01)
        userBloomFilter.tryInit(100000L, 0.01);

        // Load ID từ DB lên Bloom Filter. 
        // (Lưu ý: Thực tế nếu data lớn, bạn nên phân trang để load)
        userRepository.findAll().forEach(user -> userBloomFilter.add(user.getId()));
    }

    // Hàm dùng để check
    public boolean mightContainUser(String userId) {
        return userBloomFilter.contains(userId);
    }
    
    // Đừng quên gọi hàm này khi có 1 User MỚI được tạo/đăng ký
    public void addUser(String userId) {
        userBloomFilter.add(userId);
    }
}
```

**3. Áp dụng vào Service (Ví dụ `UserService`):**
Sửa logic lấy dữ liệu để check Bloom Filter trước tiên.

```java
@Autowired
private BloomFilterManager bloomFilterManager;

// Vẫn giữ sync = true để chống Breakdown
@Cacheable(value = "users", key = "#id", sync = true)
public UserResponse getUserById(String id){
    // 1. CHẶN ĐỨNG CACHE PENETRATION TẠI ĐÂY
    if (!bloomFilterManager.mightContainUser(id)) {
        // Chắc chắn không tồn tại, văng lỗi ngay không cần chạm DB
        throw new AppException(USER_NOT_FOUND); 
    }
    
    // 2. Đi tiếp vào DB (nếu lọt qua Bloom Filter)
    return userMapper.toResponse(userRepository.findById(id)
            .orElseThrow(()->new AppException(USER_NOT_FOUND)));
}
```
*Lưu ý với Bloom Filter: Nó chỉ có thể "Thêm vào" (Add) chứ không hỗ trợ "Xóa đi" (Delete). Nếu xóa 1 user trong DB, Bloom Filter vẫn nghĩ user đó còn tồn tại, nhưng lúc này request lọt xuống sẽ bị DB báo lỗi như bình thường - hệ thống không sao cả.*

---

## Phần 2: Xử lý Cache Avalanche (Tuyết Lở Cache) bằng Random Jitter

### Mục tiêu
Tất cả các key tạo ra cùng một lúc sẽ hết hạn cùng một lúc nếu dùng chung 1 TTL (ví dụ 60 phút). Việc cộng thêm một số phút ngẫu nhiên (Jitter) sẽ khiến chúng hết hạn rải rác, tránh việc Database bị bão request.

### Cách bạn tự thực hiện
Bạn hãy tìm đến file `RedisConfig.java` trong thư mục `config`.

**1. Tuỳ chỉnh lại cấu hình RedisCacheManager:**
Mục tiêu là tạo ra một `RedisCacheManagerBuilderCustomizer` hoặc ghi đè trực tiếp cấu hình lúc khởi tạo `CacheManager`.
- Bạn cần sửa phương thức tạo `@Bean RedisCacheManager`.
- Hãy set up một cấu hình mặc định, và có thể cấu hình riêng cho từng cache name (ví dụ "users", "carts") với thời gian sống (TTL) khác nhau cộng với random jitter.

**Ví dụ tham khảo (Cách sử dụng Random class để cộng thêm thời gian):**
```java
import java.time.Duration;
import java.util.Random;
// ... các import khác

@Bean
public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    // Khởi tạo Random để làm Jitter
    Random random = new Random();
    
    // Ví dụ: TTL cơ bản là 60 phút, cộng thêm random từ 1 đến 15 phút
    long baseMinutes = 60L;
    long jitterMinutes = random.nextInt(15) + 1; // Tạo số ngẫu nhiên từ 1 -> 15
    
    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(baseMinutes + jitterMinutes)) // Cấu hình TTL có jitter
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new JdkSerializationRedisSerializer()));

    return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            // (Thử thách thêm): Bạn có thể dùng .withCacheConfiguration("tên_cache", cấu_hình_riêng) để set TTL khác nhau cho từng loại data.
            .build();
}
```
*Hãy thử tự implement xem nhé! Bạn có thể khởi động lại app và kiểm tra (inspect) trong CLI của redis (lệnh `TTL tên_key`) để xem TTL của các key có lệch nhau không.*

---

## Phần 3: Cấu hình Eviction Policies

### Mục tiêu
Làm cho Redis không bị chết (OOM - Out of Memory) khi hết RAM, mà nó sẽ tự động xóa các dữ liệu ít quan trọng hoặc ít được truy cập nhất.

### Cách bạn tự thực hiện
Cái này bạn không code bằng Java. Nó thuộc về khâu vận hành hệ thống.

**Ví dụ tham khảo:**
Nếu bạn đang dùng máy tính cá nhân để chạy redis qua Docker, bạn có thể chạy lại container với tham số sau:
```bash
docker run -d --name redis-ecommerce -p 6379:6379 redis --maxmemory 512mb --maxmemory-policy allkeys-lru
```

Nếu bạn cài trực tiếp trên máy:
1. Bạn tìm file `redis.conf`.
2. Tìm dòng `maxmemory` và sửa thành `maxmemory 512mb` (hoặc tuỳ chọn).
3. Tìm dòng `maxmemory-policy` và sửa thành `maxmemory-policy allkeys-lru`.
4. Khởi động lại service redis.

---

Chúc bạn code thật vui! Nếu trong quá trình cài đặt gặp khó khăn (như lỗi đỏ, hay không biết test kết quả ra sao), hãy gọi tôi, tôi sẽ hướng dẫn bạn debug nhé.
