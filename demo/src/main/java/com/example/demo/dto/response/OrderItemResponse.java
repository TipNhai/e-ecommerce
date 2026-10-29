package com.example.demo.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemResponse {
    Long productId;
    String productName;
    String productThumbnail;
    BigDecimal price;     // Giá snapshot tại thời điểm mua
    Integer quantity;
    BigDecimal subTotal;
}