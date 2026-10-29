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

public class AddressResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    Long id;
    String recipientName;
    String phoneNumber;
    String province;
    String district;
    String ward;
    String addressDetail;
}

