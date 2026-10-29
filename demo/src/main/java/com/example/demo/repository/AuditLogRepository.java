package com.example.demo.repository;

import com.example.demo.entity.AuditLog;
import com.example.demo.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    Page<AuditLog> findAllByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Page<AuditLog> findAllByEntityNameAndEntityIdOrderByCreatedAtDesc(
            String entityName, String entityId, Pageable pageable
    );

    Page<AuditLog> findAllByActionOrderByCreatedAtDesc(AuditAction action, Pageable pageable);
}
