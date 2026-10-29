package com.example.demo.entity;

import com.example.demo.enums.AuditAction;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "audit_logs" , indexes = {
        @Index(name = "idx_audit_logs_user_created" , columnList = "user_id, created_at DESC"),
        @Index(name = "idx_audit_logs_entity_created" , columnList = "entity_name, entity_id, created_at DESC"),
        @Index(name = "idx_audit_logs_action_created" , columnList = "action, created_at DESC")

})
public class AuditLog implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "user_id" , length = 36)
    String userId;

    @Column(length = 100)
    String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    AuditAction action;

    // Tên bảng / Entity bị tác động (VD: Order, Product, User)
    @Column(name = "entity_name", length = 100)
    String entityName;

    @Column(name = "entity_id", length = 100)
    String entityId;

    // Mô tả chi tiết hành động
    @Column(columnDefinition = "TEXT")
    String description;

    // Dữ liệu cũ (JSON string) - Dùng khi UPDATE/DELETE
    @Column(name = "old_value", columnDefinition = "LONGTEXT")
    String oldValue;

    // Dữ liệu mới (JSON string) - Dùng khi CREATE/UPDATE
    @Column(name = "new_value", columnDefinition = "LONGTEXT")
    String newValue;

    // Địa chỉ IP của client
    @Column(name = "ip_address", length = 45)
    String ipAddress;

    // User-Agent (Browser, OS)
    @Column(name = "user_agent", length = 255)
    String userAgent;

    @CreationTimestamp
    @Column(name = "created_at" , nullable = false , updatable = false)
    LocalDateTime createdAt;
}
