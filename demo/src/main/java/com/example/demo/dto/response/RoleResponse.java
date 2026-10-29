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
public class RoleResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    String name;
    String description;
}


