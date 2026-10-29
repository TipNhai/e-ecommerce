package com.example.demo.dto.response;

import com.example.demo.enums.NotificationType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {
    Long id;
    String title;
    String message;
    NotificationType type;
    String referenceId;
    Boolean isRead;
    LocalDateTime createdAt;
}