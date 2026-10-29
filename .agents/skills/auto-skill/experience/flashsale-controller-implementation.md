# Bài học từ việc triển khai FlashsaleController

## Bối cảnh
Triển khai một API controller để phục vụ việc test Distributed Lock bằng cách gọi các phương thức trong `FlashSaleService`.

## Giải pháp
- Inject `FlashSaleService` vào trong controller qua lombok `@RequiredArgsConstructor`.
- Ánh xạ trực tiếp các tham số từ HTTP Request qua annotation `@RequestParam` tới tham số hàm của service (ví dụ: `productId`, `userId`, `newStock`, `useLock`, `totalRequest`).
- Bọc kết quả trả về trong một lớp wrapper phản hồi chung là `ApiResponse<T>`, ở đây dùng kiểu dữ liệu `String` để thông báo trạng thái của tác vụ, nhằm đảm bảo tính đồng bộ với cấu trúc response chuẩn của toàn bộ hệ thống API.

## Kinh nghiệm
- Đối với những API được thiết kế chuyên để kiểm thử kịch bản đồng thời (`runConcurrentTest`), kết quả và log chủ yếu xuất hiện ở console của server. Có thể thiết kế trả về đơn giản dưới dạng thông báo trạng thái thay vì cấu trúc JSON quá phức tạp.
- Cần chú ý đến annotation `@PostMapping` vì phần lớn các API làm thay đổi trạng thái (reset tồn kho, mua hàng, chạy test tải ghi) đều không nên sử dụng `@GetMapping`.
