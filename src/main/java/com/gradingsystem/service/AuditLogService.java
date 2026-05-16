package com.gradingsystem.service;

public interface AuditLogService {

    void log(String action, String entity, String entityId);
}
