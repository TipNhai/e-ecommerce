package com.example.demo.aspect;

import com.example.demo.annotation.Auditable;
import com.example.demo.entity.User;
import com.example.demo.service.AuditLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuditLogAspect {

    AuditLogService auditLogService;
    ObjectMapper objectMapper;

    @Around("@annotation(com.example.demo.annotation.Auditable)")
    public Object logAudit(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed(); // Thực thi method gốc

        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Auditable auditable = method.getAnnotation(Auditable.class);

            String userId = null;
            String email = null;

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                Object principal = auth.getPrincipal();

                // Trường hợp 1: Principal là Entity User triển khai UserDetails
                if (principal instanceof User user) {
                    userId = String.valueOf(user.getId()); // Hoặc user.getUserId() tùy cách đặt tên field trong Entity User
                    email = user.getEmail();
                }
            }

            // Lấy IP & User-Agent từ Request
            String ipAddress = "UNKNOWN";
            String userAgent = "UNKNOWN";
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                ipAddress = getClientIp(request);
                userAgent = request.getHeader("User-Agent");
            }

            String newValueJson = null;
            if (result != null) {
                newValueJson = objectMapper.writeValueAsString(result);
            }

            // Gọi hàm lưu bất đồng bộ
            auditLogService.saveAuditLogAsync(
                    userId,
                    email,
                    auditable.action(),
                    auditable.entityName(),
                    null, // entityId có thể trích xuất nếu cần
                    auditable.description(),
                    null,
                    newValueJson,
                    ipAddress,
                    userAgent
            );
        } catch (Exception e) {
            log.error("Lỗi khi ghi Audit Log: {}", e.getMessage());
        }

        return result;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0];
        }
        return ip;
    }
}