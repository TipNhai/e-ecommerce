package com.example.demo.dto.response;
import java.io.Serial;
import java.io.Serializable;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    String id;
    String username;
    String email;
    String fullName;
    String phoneNumber;
    String avatar;
    LocalDate dob;
    String gender;
    Set<AddressResponse> addresses;
}


