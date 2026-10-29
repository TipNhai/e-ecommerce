package com.example.demo.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest {
    @Size(min = 4 , message = "INVALID_USERNAME")
    String username;
    @Size(min = 8 , message = "INVALID_PASSWORD")
    String password;
    @Size(min = 8 , message = "INVALID_PASSWORD")
    String confirmPassword;
    String email;
    String fullName;
    String phoneNumber;
}
