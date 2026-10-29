package com.example.demo.dto.request;

import com.example.demo.enums.AuditAction;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuditLogFilterRequest {
    String userId;
    AuditAction action;
    String entityName;
    String entityId;
    LocalDateTime fromDate;
    LocalDateTime toDate;
}