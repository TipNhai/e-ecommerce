package com.example.demo.dto.request;

import com.example.demo.enums.DiscountType;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateCouponRequest {

    @NotBlank(message = "Coupon code is required")
    @Size(max = 50)
    String code;

    @NotBlank(message = "Description is required")
    String description;

    @NotNull(message = "Discount type is required")
    DiscountType discountType;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
    BigDecimal discountValue;

    BigDecimal minOrderValue;
    BigDecimal maxDiscountAmount;

    @NotNull(message = "Usage limit is required")
    @Min(value = 1, message = "Usage limit must be at least 1")
    Integer usageLimit;

    @NotNull(message = "Start date is required")
    LocalDateTime startDate;

    @NotNull(message = "End date is required")
    LocalDateTime endDate;
}