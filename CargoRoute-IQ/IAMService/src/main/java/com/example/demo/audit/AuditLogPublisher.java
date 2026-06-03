package com.example.demo.audit;

import org.springframework.stereotype.Component;

import com.example.demo.DTO.AuditLogDTO;
import com.example.demo.services.AuditLogService;

@Component
public class AuditLogPublisher {

    private final AuditLogService auditLogService;

    public AuditLogPublisher(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    public void log(AuditLogDTO dto) {
        auditLogService.saveAuditLog(dto);  // ✅ FIXED
    }
}