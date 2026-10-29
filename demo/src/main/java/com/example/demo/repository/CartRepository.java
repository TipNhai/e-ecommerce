package com.example.demo.repository;

import com.example.demo.entity.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    //lấy cả cartItem và product
    @EntityGraph(attributePaths = {"cartItems", "cartItems.product"})
    Optional<Cart> findByUserId(String userId);

    // Xóa Cache giỏ hàng khi lưu/xóa Cart
    @Override
    <S extends Cart> S save(S entity);

}
