package com.example.demo.mapper;

import com.example.demo.dto.response.AuditLogResponse;
import com.example.demo.entity.AuditLog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {
    AuditLogResponse toResponse(AuditLog auditLog);
}
