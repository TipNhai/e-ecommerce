package com.example.demo.service;

import com.example.demo.dto.request.AuditLogFilterRequest;
import com.example.demo.dto.response.AuditLogResponse;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.entity.AuditLog;
import com.example.demo.enums.AuditAction;
import com.example.demo.mapper.AuditLogMapper;
import com.example.demo.repository.AuditLogRepository;
import com.example.demo.specification.AuditLogSpecification;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuditLogService {

    AuditLogRepository auditLogRepository;
    AuditLogMapper auditLogMapper;

    // 1. Lưu log bất đồng bộ (chạy nền không blocking request chính)
    @Async
    @Transactional
    public void saveAuditLogAsync(
            String userId,
            String email,
            AuditAction action,
            String entityName,
            String entityId,
            String description,
            String oldValue,
            String newValue,
            String ipAddress,
            String userAgent
    ) {
        AuditLog log = AuditLog.builder()
                .userId(userId)
                .email(email)
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .description(description)
                .oldValue(oldValue)
                .newValue(newValue)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        auditLogRepository.save(log);
    }

    // 2. Tìm kiếm / Lọc log phân trang cho Admin
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> filterAuditLogs(AuditLogFilterRequest filter, int page, int size) {
        // Sắp xếp mặc định theo createdAt DESC truyền thẳng vào Pageable
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // Gọi Specification đã tách riêng
        Specification<AuditLog> spec = AuditLogSpecification.filter(filter);

        Page<AuditLog> logPage = auditLogRepository.findAll(spec, pageable);

        return PageResponse.<AuditLogResponse>builder()
                .pageNo(page)
                .pageSize(size)
                .totalPages(logPage.getTotalPages())
                .totalElements(logPage.getTotalElements())
                .content(logPage.getContent().stream().map(auditLogMapper::toResponse).toList())
                .build();
    }
}