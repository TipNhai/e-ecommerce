package com.example.demo.dto.response;
import java.io.Serial;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String slug;
    private BigDecimal price;
    private Long discountPrice;
    private String thumbnail;
    private String status;

    // Chi tiết Category gọn gàng (không lấy nguyên Object Category nặng)
    private Long categoryId;
    private String categoryName;

    // Chi tiết Brand gọn gàng
    private Long brandId;
    private String brandName;
}


