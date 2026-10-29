package com.example.demo.controller;

import com.example.demo.dto.request.AuditLogFilterRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.AuditLogResponse;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.service.AuditLogService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    AuditLogService auditLogService;

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> filterAuditLogs(
            @RequestBody AuditLogFilterRequest filterRequest,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.<PageResponse<AuditLogResponse>>builder()
                        .result(auditLogService.filterAuditLogs(filterRequest, page, size))
                        .build()
        );
    }
}