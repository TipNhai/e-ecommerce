package com.example.demo.dto.request;
import jakarta.validation.constraints.NotNull;
import lombok.*;


@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
//Không có id vì khi có bearer thì sẽ biết ai là người dùng tránh lỗi bảo mật
public class AddFavoriteRequest {
    @NotNull(message = "ProductId không được để trống")
    Long productId;
}
