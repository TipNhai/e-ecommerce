package com.example.demo.dto.response;
import java.io.Serial;
import java.io.Serializable;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String slug;
    private String description; // Mô tả chi tiết chỉ trả về ở đây
    private BigDecimal price;
    private Long discountPrice;
    private Long quantity;    // Số lượng tồn kho
    private String thumbnail;
    private String status;

    // Lồng ghép DTO nhỏ của Category & Brand để UI hiển thị thông tin nâng cao
    private CategoryResponse category;
    private BrandResponse brand;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


