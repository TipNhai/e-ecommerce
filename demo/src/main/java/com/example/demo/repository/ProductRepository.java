package com.example.demo.repository;

import com.example.demo.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {
    //Nên thay đổi trực tiếp dưới db tránh nhiều người dùng cùng cập nhật cùng lúc
    @Modifying
    @Query("UPDATE Product p SET p.quantity = p.quantity + :delta WHERE p.id = :id AND (p.quantity + :delta) >= 0")
    void updateQuantity(@Param("id")Long id, @Param("delta") int delta);
    boolean existsBySlug(String slug);

}
