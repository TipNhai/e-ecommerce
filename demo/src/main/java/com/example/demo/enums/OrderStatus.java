package com.example.demo.enums;

public enum OrderStatus {
    PENDING,        // Chờ thanh toán / Chờ xác nhận
    CONFIRMED,      // Đã xác nhận
    PROCESSING,     // Đang đóng gói
    SHIPPED,        // Đang giao hàng
    DELIVERED,      // Giao thành công
    CANCELLED       // Đã hủy
}
