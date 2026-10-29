package com.example.demo.entity;

import com.example.demo.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Data
@Getter
@Setter
@Entity
@Table(name = "notifications", indexes = {
        // 1. Phục vụ: đếm chưa đọc (user_id, is_read) hoặc lọc tab "Chưa đọc" theo thời gian
        @Index(
                name = "idx_notifications_user_read_created",
                columnList = "user_id, is_read, created_at DESC"
        ),
        // 2. Phục vụ: tab "Tất cả thông báo" (user_id, created_at DESC) - vì query này KHÔNG có is_read
        @Index(
                name = "idx_notifications_user_created",
                columnList = "user_id, created_at DESC"
        )
})
public class Notification implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(nullable = false, length = 150)
    String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    NotificationType type;

    // Lưu ID thực thể liên quan để Client click vào điều hướng (VD: orderId, couponCode)
    String referenceId;

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    Boolean isRead = false;

    @CreationTimestamp
    LocalDateTime createdAt;
}
