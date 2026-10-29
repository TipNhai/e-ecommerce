# Lỗi "Order not found" do nhầm lẫn giữa auth.getName() và userId UUID

## Nguyên nhân
Trong Spring Security, khi lấy thông tin người dùng từ `SecurityContextHolder`:
```java
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String userId = auth.getName();
```
Hàm `auth.getName()` mặc định sẽ trỏ tới hàm `getUsername()` của interface `UserDetails`. Do đó, nó sẽ trả về **username** (ví dụ: "admin", "nva123") chứ KHÔNG PHẢI trả về **id** (UUID) của Entity User, kể cả khi bạn đã đổi subject của JWT thành UUID!

Do đó, khi gọi repository:
```java
orderRepository.findByIdAndUserId(orderId, userId)
```
Hệ thống sẽ thực thi câu lệnh SQL với điều kiện `order.user.id = 'admin'`, dẫn đến không tìm thấy kết quả và quăng lỗi "Order not found" (dù Order ID đó thực sự tồn tại và thuộc về user đang đăng nhập).

## Bài học & Cách giải quyết
Thay vì dùng `auth.getName()`, bắt buộc phải ép kiểu Principal về đúng Entity `User` của dự án để gọi hàm `getId()`:
```java
String userId = null;
if (auth != null && auth.getPrincipal() instanceof com.example.demo.entity.User) {
    userId = ((com.example.demo.entity.User) auth.getPrincipal()).getId();
}
```
Luôn cẩn trọng khi dùng `auth.getName()` nếu khóa chính (ID) của hệ thống không phải là `username`.
