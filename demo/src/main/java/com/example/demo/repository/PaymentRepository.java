package com.example.demo.repository;

import com.example.demo.entity.Payment;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface PaymentRepository extends JpaRepository<Payment,Long> {
    @EntityGraph(attributePaths = {"order" , "order.user"})
    Optional<Payment> findByReferenceCode(String referenceCode);

    @EntityGraph(attributePaths = {"order"})
    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> getByOrderId(@NotNull(message = "Order ID is required") Long orderId);
}
