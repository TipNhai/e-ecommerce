package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateAddressRequest {
    @NotBlank(message = "Tên người nhận không được để trống")
    String recipientName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(
            regexp = "^(0|\\+84)\\d{9}$",
            message = "Số điện thoại phải bắt đầu bằng 0 hoặc +84 và theo sau là đúng 9 chữ số"
    )
    String phoneNumber;

    @NotBlank(message = "Tỉnh/Thành phố không được để trống")
    String province;

    @NotBlank(message = "Quận/Huyện không được để trống")
    String district;

    @NotBlank(message = "Phường/Xã không được để trống")
    String ward;

    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    String addressDetail;

    Boolean isDefault;

}
