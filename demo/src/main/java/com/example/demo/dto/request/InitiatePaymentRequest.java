package com.example.demo.dto.request;


import com.example.demo.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InitiatePaymentRequest {

    @NotNull(message = "Order ID is required")
    Long orderId;

    @NotNull(message = "Payment method is required")
    PaymentMethod method;

    // Tùy chọn ngân hàng nội địa (nếu chọn VNPAY: NCB, VCB, CTG...)
    String bankCode;
}