package com.example.demo.dto.response.momo;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MomoCreatePaymentResponse {
    String partnerCode;
    String orderId;
    String requestId;
    Long amount;
    Long responseTime;
    String message;
    Integer resultCode;
    String payUrl;       // Link thanh toán mở web MoMo hoặc App
    String deeplink;     // Link mở trực tiếp App MoMo trên mobile
    String qrCodeUrl;    // URL ảnh QR
}