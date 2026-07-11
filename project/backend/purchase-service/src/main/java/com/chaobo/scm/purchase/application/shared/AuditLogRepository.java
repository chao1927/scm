package com.chaobo.scm.purchase.application.shared;

public interface AuditLogRepository {

    void save(
            CommandContext context,
            String operation,
            String targetType,
            long targetId,
            String targetNo,
            String beforeSnapshot,
            String afterSnapshot);
}
