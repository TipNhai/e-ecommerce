package com.example.demo.entity;

import com.example.demo.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE )
@Builder
@Entity
public class Coupon implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false , unique = true , length = 50)
    String code;

    String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false , length = 20)
    DiscountType discountType;

    @Column(nullable = false, precision = 15, scale = 2)
    BigDecimal discountValue; // Giá trị % hoặc số tiền

    @Column(precision = 15, scale = 2)
    BigDecimal minOrderValue; // Giá trị đơn hàng tối thiểu để áp dụng

    @Column(precision = 15, scale = 2)
    BigDecimal maxDiscountAmount; // Số tiền giảm tối đa (khi dùng PERCENTAGE)

    @Column(nullable = false)
    Integer usageLimit; // Tổng lượt dùng tối đa

    @Builder.Default
    @Column(nullable = false)
    Integer usedCount = 0; // Số lượt đã sử dụng

    @Column(nullable = false)
    LocalDateTime startDate;

    @Column(nullable = false)
    LocalDateTime endDate;

    @Builder.Default
    @Column(nullable = false)
    Boolean isActive = true;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

    public boolean isValidForOrder(BigDecimal orderTotal){
        LocalDateTime now = LocalDateTime.now();
        if(!isActive || now.isBefore(startDate) || now.isAfter(endDate))
            return false;
        if(usedCount>usageLimit)
            return false;
        return minOrderValue == null || orderTotal.compareTo(minOrderValue) >= 0;
    }

    public BigDecimal calculateDiscount(BigDecimal orderTotal) {
        if (discountType == DiscountType.FIXED_AMOUNT) {
            return discountValue.min(orderTotal);
        } else {
            BigDecimal discount = orderTotal.multiply(discountValue).divide(BigDecimal.valueOf(100));
            if (maxDiscountAmount != null && discount.compareTo(maxDiscountAmount) > 0) {
                return maxDiscountAmount;
            }
            return discount.min(orderTotal);
        }
    }
}
