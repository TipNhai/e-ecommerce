package com.example.demo.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {
    @Size(max = 255, message = "Tên sản phẩm tối đa 255 ký tự")
    private String name;

    private String description;

    @Min(value = 0, message = "Giá sản phẩm phải lớn hơn hoặc bằng 0")
    private BigDecimal price;

    @Min(value = 0, message = "Giá khuyến mãi phải lớn hơn hoặc bằng 0")
    private Long discountPrice;

    private Long quantity;

    private String thumbnail;

    private String status;

    private Long categoryId;

    private Long brandId;
}
