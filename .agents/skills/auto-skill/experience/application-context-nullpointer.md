# Bài học từ lỗi NullPointerException với ApplicationContext

## Bối cảnh
Khi gọi API test (sử dụng method `runConcurrentTest` trong `FlashSaleService`), hệ thống văng lỗi `NullPointerException` tại dòng:
`FlashSaleService selfProxy = applicationContext.getBean(FlashSaleService.class);`
Nguyên nhân là do biến `applicationContext` đang bị `null`.

## Nguyên nhân gốc rễ
- Lớp `FlashSaleService` có sử dụng annotation `@RequiredArgsConstructor` của Lombok nhằm tự động inject các dependency.
- Annotation `@RequiredArgsConstructor` của Lombok chỉ tạo constructor tiêm các tham số có bổ nghĩa `final` (hoặc `@NonNull`).
- Biến `applicationContext` lại được khai báo là: `private ApplicationContext applicationContext;` (thiếu từ khóa `final`).
- Do đó Spring không tự động inject `ApplicationContext` vào constructor, dẫn đến giá trị mặc định của biến là `null`.

## Cách khắc phục
- Sửa lại khai báo biến thành `private final ApplicationContext applicationContext;`.
- Sau khi thêm `final`, Lombok sẽ đưa tham số này vào hàm tạo và Spring IoC sẽ tự động tiêm giá trị khi khởi tạo Bean.

## Kinh nghiệm
- Khi sử dụng DI qua Constructor với Lombok `@RequiredArgsConstructor`, luôn phải đảm bảo các dependency cần tiêm được khai báo với từ khóa `final`.
- Lỗi này thường phổ biến với những trường hợp mới thêm các service như `ApplicationContext`, `HttpServletRequest`... mà quên thêm `final`.
