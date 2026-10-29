package com.example.demo.listener;

import com.example.demo.entity.AuditLog;
import com.example.demo.event.AuditEvent;
import com.example.demo.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditLogConsumer {
    private final AuditLogRepository auditLogRepository;

    @KafkaListener(topics = "audit_events_topic" , groupId = "ecommerce-audit-group")
    public void handleAuditEvents(AuditEvent event , Acknowledgment ack){
        AuditLog auditLog = AuditLog.builder()
                .userId(event.getActorId())
                .action(event.getAction())
                .entityName(event.getEntityName())
                .description(event.getDetails())
                .build();

        auditLogRepository.save(auditLog);
        ack.acknowledge();
        log.info("Đã lưu auditLog cho hành động {}",event.getAction());
    }
}
