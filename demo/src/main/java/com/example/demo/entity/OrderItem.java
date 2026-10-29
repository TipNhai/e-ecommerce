package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class OrderItem implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    Order order;
    @ManyToOne(fetch = FetchType.LAZY)
    Product product;
    Integer quantity;
    BigDecimal price;

    // Snapshot sản phẩm tránh trường hợp sản phẩm bị xoá hoặc thay đổi
    String productName;
    String productThumbnail;
}
