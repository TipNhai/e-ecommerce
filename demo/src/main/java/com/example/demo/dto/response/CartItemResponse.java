package com.example.demo.dto.response;
import java.io.Serial;
import lombok.*;

import java.math.BigDecimal;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    Long id;
    Long productId;
    String productName;
    String productThumbnail;
    BigDecimal unitPrice;
    Integer quantity;
    BigDecimal subTotal; // unitPrice * quantity
}


