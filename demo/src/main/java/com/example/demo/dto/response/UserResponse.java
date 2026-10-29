package com.example.demo.dto.response;
import java.io.Serial;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    String id;
    String username;
    String fullName;
    String avatar;
    RoleResponse roles;
}


