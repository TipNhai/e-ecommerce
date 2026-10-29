package com.example.demo.dto.request;


import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateAddressRequest {
    String recipientName;

    @Pattern(
            regexp = "^(0|\\+84)\\d{9}$",
            message = "Số điện thoại phải bắt đầu bằng 0 hoặc +84 và theo sau là đúng 9 chữ số"
    )
    String phoneNumber;

    String province;
    String district;
    String ward;
    String addressDetail;
    Boolean isDefault;
}