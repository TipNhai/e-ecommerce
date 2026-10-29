# Lỗi "You do not have permission" sau khi đổi Subject JWT từ username sang userId

## Nguyên nhân 
1. **Lỗi Token Refresh trên Frontend / Redis**: Khi đổi Subject JWT từ `username` sang `userId` (dạng UUID), token cũ vẫn còn hiệu lực trong Redis hoặc bộ nhớ trình duyệt/Postman. Khi gọi `/refresh-token` với `refreshToken` cũ, hệ thống giải mã ra subject là `username` thay vì `userId`. Sau đó, nó tạo ra `accessToken` MỚI nhưng vẫn chứa subject là `username`. Lúc gọi API xác thực bằng `accessToken` mới này, `UserDetailsServiceImpl` nhận vào `username` để truy vấn qua `findById(uuid)` gây ra lỗi không tìm thấy người dùng.
2. **Class `User` chưa implements `UserDetails`**: Dù token có đúng subject là `userId` thì dòng lệnh `(UserDetails) userRepository.findById(userId).get()` vẫn sẽ quăng ra `ClassCastException` nếu `User` chưa implement interface `UserDetails`. Exception này bị Filter bắt lại, làm mất Context xác thực.
3. **Thứ tự truyền nhầm Token (AccessToken vs RefreshToken)**: Lỗi "You do not have permission" tương ứng với `AppException(ErrorCode.UNAUTHORIZED)` từ hàm `verifyAccessToken`. Hàm này sẽ văng lỗi nếu signature sai hoặc token hết hạn. Điều này thường xảy ra nếu người dùng vô tình truyền **chuỗi refreshToken** vào Bearer header (chữ ký không khớp với `accessKey`), hoặc gửi token cũ đã quá hạn 30 phút.

## Bài học và Giải pháp
- Phải implement `UserDetails` trên `User` class (bắt buộc trong Spring Security).
- Xóa bộ đệm redis (FLUSHALL) và đăng nhập lại mới (clear cookies/localStorage) mỗi khi thay đổi cấu trúc của Payload/Subject trong JWT.
- Đảm bảo Postman không tự động đính kèm `refreshToken` thay vì `accessToken`.
