package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RefreshTokenRequest {
    @NotBlank(message = "Refresh Token không được để trống")
    String refreshToken;
    // Tùy chọn: FE truyền lên nếu chủ động refresh khi Access Token cũ chưa hết hạn
    private String accessToken;
}
