package com.example.demo.repository;

import com.example.demo.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Khớp tuyệt đối tiền tố (user_id, is_read) -> Query cực nhanh O(log N)
    long countByUserIdAndIsReadFalse(String userId);

    // Khớp toàn bộ Composite Index (user_id, is_read, created_at DESC)
    // Dùng khi User chọn tab "Chưa đọc" trên UI
    Page<Notification> findAllByUserIdAndIsReadFalseOrderByCreatedAtDesc(String userId, Pageable pageable);

    // Dùng cho tab "Tất cả thông báo" (sẽ ăn theo index idx_notifications_user_created)
    Page<Notification> findAllByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    // Khớp 2 cột đầu của index
    // @Modifying báo cho Spring Data JPA gọi Query.executeUpdate(). Nếu thiếu annotation này, ứng dụng sẽ ném ra lỗi InvalidDataAccessApiUsageException.
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") String userId);

    // Tìm 1 thông báo
    Optional<Notification> findByIdAndUserId(Long id, String userId);
}
