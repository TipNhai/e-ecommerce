package com.example.demo.specification;

import com.example.demo.dto.request.AuditLogFilterRequest;
import com.example.demo.entity.AuditLog;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AuditLogSpecification {

    private AuditLogSpecification() {
        // Private constructor để tránh new instance (Chuẩn SonarLint cho Utility Class)
    }

    public static Specification<AuditLog> filter(AuditLogFilterRequest filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            addEqualsIfHasText(predicates, cb, root.get("userId"), filter.getUserId());
            addEqualsIfNotNull(predicates, cb, root.get("action"), filter.getAction());
            addEqualsIfHasText(predicates, cb, root.get("entityName"), filter.getEntityName());
            addEqualsIfHasText(predicates, cb, root.get("entityId"), filter.getEntityId());

            addRangePredicates(predicates, cb, root, filter);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // --- HELPER METHODS GIÚP GIẢM COGNITIVE COMPLEXITY ---

    private static void addEqualsIfHasText(List<Predicate> predicates, CriteriaBuilder cb,
                                           jakarta.persistence.criteria.Path<Object> path, String value) {
        if (StringUtils.hasText(value)) {
            predicates.add(cb.equal(path, value));
        }
    }

    private static void addEqualsIfNotNull(List<Predicate> predicates, CriteriaBuilder cb,
                                           jakarta.persistence.criteria.Path<Object> path, Object value) {
        if (Objects.nonNull(value)) {
            predicates.add(cb.equal(path, value));
        }
    }

    private static void addRangePredicates(List<Predicate> predicates, CriteriaBuilder cb,
                                           Root<AuditLog> root, AuditLogFilterRequest filter) {
        if (Objects.nonNull(filter.getFromDate())) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getFromDate()));
        }
        if (Objects.nonNull(filter.getToDate())) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getToDate()));
        }
    }
}