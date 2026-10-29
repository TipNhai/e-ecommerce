package com.example.demo.service;

import com.example.demo.dto.response.NotificationResponse;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.entity.Notification;
import com.example.demo.entity.User;
import com.example.demo.enums.NotificationType;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.NotificationMapper;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class NotificationService {
    NotificationRepository notificationRepository;
    UserRepository userRepository;
    NotificationMapper notificationMapper;

    @Async
    @Transactional
    public void createNotification(String userId, String title, String message, NotificationType type, String referenceId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .referenceId(referenceId)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    // Tinh chỉnh: Cho phép Client truyền flag `unreadOnly` để chọn index tối ưu nhất
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getUserNotifications(String userId, boolean unreadOnly, int page, int size){
        Pageable pageable = PageRequest.of(page - 1 , size);
        Page<Notification> notificationPage;
        if(unreadOnly){
            notificationPage =  notificationRepository.findAllByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId,pageable);
        } else {
            // Tận dụng index: idx_notifications_user_created
            notificationPage =  notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable);
        }
        return PageResponse.<NotificationResponse>builder()
                .pageNo(page)
                .pageSize(size)
                .totalPages(notificationPage.getTotalPages())
                .totalElements(notificationPage.getTotalElements())
                .content(notificationPage.getContent().stream().map(notificationMapper::toResponse).toList())
                .build();
    }
    @Transactional(readOnly = true)
    public long getUnreadCount(String userId) {
        // Tận dụng index: idx_notifications_user_read_created
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(String userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (Boolean.FALSE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void markAllAsRead(String userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }
}
