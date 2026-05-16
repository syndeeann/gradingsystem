package com.gradingsystem.service.impl;

import com.gradingsystem.entity.AuditLog;
import com.gradingsystem.repository.AuditLogRepository;
import com.gradingsystem.service.AuditLogService;
import com.gradingsystem.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String entity, String entityId) {
        String userId = tryGetCurrentUserId();
        auditLogRepository.save(AuditLog.builder()
            .userId(userId)
            .action(action)
            .entity(entity)
            .entityId(entityId)
            .build());
    }

    private String tryGetCurrentUserId() {
        try {
            return SecurityUtils.getCurrentUserId();
        } catch (Exception e) {
            return null;
        }
    }
}
