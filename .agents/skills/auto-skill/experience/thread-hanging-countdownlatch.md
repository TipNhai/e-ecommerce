# Bài học từ lỗi Thread Hanging và Hardcode String trong FlashSale

## Bối cảnh
Khi chạy hàm `runConcurrentTest` trong `FlashSaleService` với tham số `useLock=true`:
1. Log luôn in ra `[NoLock]` mặc dù đang gọi hàm `buyWithLock`.
2. API bị treo (hanging), không trả về response.

## Nguyên nhân gốc rễ
1. **Lỗi Hardcode String:** Trong hàm `buyWithLock`, logic gọi tới hàm dùng chung bị hardcode tham số `mode` là `"NoLock"` thay vì `"Lock"`:
   ```java
   public void buyWithLock(String productId, String userId){
       processPurchase(productId,userId,"NoLock"); // Sai ở đây
   }
   ```
2. **Lỗi Thread Hanging do sai phạm vi block `for`:** Logic khởi chạy đồng thời (`startLatch.countDown()`) và chờ hoàn thành (`endLatch.await()`) bị đặt **BÊN TRONG** vòng lặp `for` nạp task. 
   Do đó, ngay ở vòng lặp đầu tiên (`i=0`), hệ thống vừa `submit` 1 task xong đã gọi `endLatch.await()`. Lệnh này block main thread chờ cho tất cả `totalRequest` task hoàn tất, nhưng thực tế chỉ mới có 1 task được nạp vào executor. Suy ra: main thread treo vô thời hạn (deadlock).

## Cách khắc phục
1. **Sửa String:** Đổi thành `processPurchase(productId,userId,"Lock");` trong `buyWithLock`.
2. **Sửa phạm vi vòng lặp `for`:** Tách phần cấu hình `startLatch`, `endLatch` và `executorService.shutdown()` ra **BÊN NGOÀI** (sau khi kết thúc) vòng lặp `for` nạp task.

## Kinh nghiệm
- Khi test Multi-threading với `CountDownLatch`, khối lệnh `.await()` **BẮT BUỘC** phải luôn được đặt bên ngoài vòng lặp nạp các thread con. Việc đặt bên trong sẽ dẫn tới hiện tượng block ngay lập tức khiến cho các thread tiếp theo không thể được đưa vào Pool.
- Tuyệt đối cẩn thận với lỗi Copy-Paste làm lặp lại các magic string (như `"NoLock"`) mà không kiểm tra lại.
