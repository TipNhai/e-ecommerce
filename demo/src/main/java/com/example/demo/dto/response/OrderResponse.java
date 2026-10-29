package com.example.demo.dto.response;

import com.example.demo.enums.OrderStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    Long id;
    String orderCode;
    OrderStatus status;
    BigDecimal subtotal;
    BigDecimal discountAmount;
    BigDecimal finalAmount;
    String shippingAddress; // Lưu snapshot địa chỉ chuỗi
    String recipientName;
    String recipientPhone;
    String note;
    LocalDateTime createdAt;
    List<OrderItemResponse> orderItems;
}