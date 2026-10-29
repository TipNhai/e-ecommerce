# Bài học từ lỗi Lock Timeout hàng loạt trong kiểm thử đồng thời

## Bối cảnh
Khi chạy hàm `runConcurrentTest` trong `FlashSaleService` với 20 request đồng thời (`totalRequest=20`) có sử dụng khoá (`useLock=true`), kết quả trả về chỉ có 1 request mua thành công, 19 request còn lại bị lỗi timeout. Tổng thời gian thực thi rất nhanh (chỉ khoảng 60ms).

## Nguyên nhân gốc rễ
1. **Thiết lập mặc định của `@DistributedLock` không phù hợp:** 
   Annotation `@DistributedLock` có các giá trị mặc định:
   ```java
   long waitTime() default 5;
   TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
   ```
   Điều này có nghĩa là khi một thread không lấy được khoá, nó chỉ chờ tối đa **5 milliseconds** rồi sẽ văng exception (timeout).
2. **Logic xử lý chiếm dụng khoá lâu:** 
   Hàm `processPurchase` có mô phỏng logic tốn thời gian bằng lệnh `Thread.sleep(50)` (50 milliseconds).
3. **Kết hợp lại:** 
   Thread đầu tiên lấy được khoá và chiếm giữ nó trong vòng ít nhất 50ms. Trong khi đó, 19 threads khác đến cùng lúc nhưng chỉ kiên nhẫn chờ 5ms. Kết quả là cả 19 threads đều bỏ cuộc (timeout) trước khi thread đầu tiên kịp xử lý xong và nhả khoá.

## Cách khắc phục
Cần điều chỉnh thời gian chờ (`waitTime`) của khoá sao cho đủ lớn để các thread có thể xếp hàng chờ đợi nhau hoàn tất (20 requests x 50ms = tối thiểu 1000ms chờ đợi cho request cuối cùng).
Cách đơn giản nhất là đổi đơn vị tính thành giây (`TimeUnit.SECONDS`):
```java
@DistributedLock(keyPrefix = "flshsale:product:", key = "#productId", waitTime = 5, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
```
Sau thay đổi này, các thread sẽ chịu khó chờ tối đa 5 giây để lấy khoá (đủ dư dả cho toàn bộ 20 requests thực thi). Đồng thời `leaseTime` mặc định là 10 cũng sẽ trở thành 10 giây, tránh việc khoá bị nhả quá sớm.

## Kinh nghiệm
- Khi sử dụng Distributed Lock, **bắt buộc** phải kiểm tra và cấu hình lại `waitTime` và `leaseTime` cho phù hợp với nghiệp vụ.
- `waitTime` phải luôn lớn hơn tổng thời gian (worst-case scenario) mà các thread khác có thể chiếm dụng khoá. Nếu có quá nhiều request đồng thời, thời gian chờ sẽ cộng dồn nên đơn vị Milliseconds hiếm khi nào đủ an toàn.
