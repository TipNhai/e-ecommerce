package com.example.demo.specification;

import com.example.demo.dto.request.ProductFilterParam;
import com.example.demo.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;


public class ProductSpecification {

    private ProductSpecification() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }
    public static Specification<Product> filter(ProductFilterParam param) {
        return (root, query, cb) -> {
            // nơi chứa điều kiện
            List<Predicate> predicates = new ArrayList<>();

            // 1. Lọc theo Category
            if (param.getCategoryId() != null) {
                // root có thể dùng bảng Product (Specification<Product>)
                // cb dùng để làm các phép so sánh , Like , and như trong sql
                // query Quản lý cấu trúc bao quát của toàn bộ câu lệnh SQL (SELECT DISTINCT, GROUP BY...) rất ít khi dùng
                predicates.add(cb.equal(root.get("category").get("id"), param.getCategoryId()));
            }

            // 2. Lọc theo Brand
            if (param.getBrandId() != null) {
                predicates.add(cb.equal(root.get("brand").get("id"), param.getBrandId()));
            }

            // 3. Lọc theo khoảng giá (Min - Max)
            if (param.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), param.getMinPrice()));
            }
            if (param.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), param.getMaxPrice()));
            }

            // 4. Tìm kiếm theo tên sản phẩm (LIKE %search%)
            if (StringUtils.hasText(param.getSearch())) {
                String searchLike = "%" + param.getSearch().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), searchLike));
            }

            // Gom tất cả điều kiện lại bằng AND
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}