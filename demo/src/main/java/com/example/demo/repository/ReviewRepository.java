package com.example.demo.repository;

import com.example.demo.entity.Review;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review,Long> {
    @EntityGraph(attributePaths = {"user", "images"})
    Page<Review> findAllByProductId(Long productId, Pageable pageable);

    boolean existsByOrderIdAndProductId(Long orderId, Long productId);

    @EntityGraph(attributePaths = {"user" , "images"})
    Optional<Review>findByIdAndUserId(Long reviewId,String userId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double calculateAverageRating(@Param("productId") Long productId);
}
