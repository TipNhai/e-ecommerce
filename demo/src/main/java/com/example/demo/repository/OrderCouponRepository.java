package com.example.demo.repository;

import com.example.demo.entity.OrderCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderCouponRepository extends JpaRepository<OrderCoupon, Long> {
    List<OrderCoupon> findAllByOrderId(Long orderId);
}