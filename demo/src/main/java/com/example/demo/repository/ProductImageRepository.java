package com.example.demo.repository;

import com.example.demo.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage,Long> {
    Optional<ProductImage> findByIdAndProductId(Long imageId, Long productId);
}
