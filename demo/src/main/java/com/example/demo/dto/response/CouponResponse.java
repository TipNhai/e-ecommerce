package com.example.demo.dto.response;

import com.example.demo.enums.DiscountType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CouponResponse {
    Long id;
    String code;
    String description;
    DiscountType discountType;
    BigDecimal discountValue;
    BigDecimal minOrderValue;
    BigDecimal maxDiscountAmount;
    Integer usageLimit;
    Integer usedCount;
    LocalDateTime startDate;
    LocalDateTime endDate;
    Boolean isActive;
}