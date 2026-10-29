package com.example.demo.dto.response;
import java.io.Serial;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

import java.io.Serializable;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    Long id;
    String userId;
    List<CartItemResponse> cartItems;
    Integer totalQuantity;
    BigDecimal totalPrice;
}


