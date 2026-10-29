package com.example.demo.dto.response;


import com.example.demo.enums.PaymentMethod;
import com.example.demo.enums.PaymentStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentResponse {

    Long paymentId;
    String orderCode;
    PaymentMethod method;
    PaymentStatus status;
    BigDecimal amount;
    String transactionId;

    String referenceCode;
    LocalDateTime paidAt;

    // URL chuyển hướng sang cổng VNPAY / MoMo (nếu là COD/Bank thì url = null)
    String paymentUrl;

    // Hướng dẫn nếu là Chuyển khoản ngân hàng (QR/STK)
    String paymentInstructions;
}