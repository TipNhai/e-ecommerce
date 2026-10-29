package com.example.demo.dto.response;
import java.io.Serial;
import java.io.Serializable;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    Long id;
    String name;
    String slug;
    String description;
    String status;
}


