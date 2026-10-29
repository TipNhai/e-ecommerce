package com.example.demo.dto.request;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;


@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
//Không có id vì khi có bearer thì sẽ biết ai là người dùng tránh lỗi bảo mật
public class AddToCartRequest {
    @NotNull(message = "ProductId không được để trống")
    Long productId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    Integer quantity;
}
