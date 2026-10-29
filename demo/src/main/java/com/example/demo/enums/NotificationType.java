package com.example.demo.enums;

public enum NotificationType {
    ORDER,// Cập nhật trạng thái đơn hàng
    PAYMENT,// Kết quả thanh toán (VNPAY/MoMo thành công, thất bại)
    PROMOTION,// Tặng coupon mới, flash sale
    SYSTEM;// Thông báo chung từ ban quản trị
}
