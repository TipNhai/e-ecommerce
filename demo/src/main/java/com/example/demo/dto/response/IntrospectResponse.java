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
public class IntrospectResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    boolean valid;
}


