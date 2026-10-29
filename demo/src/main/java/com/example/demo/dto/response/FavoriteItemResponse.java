package com.example.demo.dto.response;
import java.io.Serial;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class FavoriteItemResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    Long productId;
    String productName;
    String productThumbnail;
    BigDecimal price;
}


