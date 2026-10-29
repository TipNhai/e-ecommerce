package com.example.demo.dto.response;

import com.example.demo.enums.AuditAction;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuditLogResponse {
    Long id;
    String userId;
    String email;
    AuditAction action;
    String entityName;
    String entityId;
    String description;
    String oldValue;
    String newValue;
    String ipAddress;
    String userAgent;
    LocalDateTime createdAt;
}