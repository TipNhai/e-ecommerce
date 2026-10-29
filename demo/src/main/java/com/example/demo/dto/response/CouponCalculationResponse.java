package com.example.demo.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CouponCalculationResponse {
    String code;
    BigDecimal originalTotal;
    BigDecimal discountAmount;
    BigDecimal finalTotal;
}