package com.example.demo.enums;

public enum PaymentStatus {
    PENDING,    // Đang chờ thanh toán
    SUCCESS,    // Giao dịch thành công
    FAILED,     // Giao dịch thất bại
    CANCELLED,  // Người dùng hủy giao dịch
    REFUNDED    // Đã hoàn tiền
}