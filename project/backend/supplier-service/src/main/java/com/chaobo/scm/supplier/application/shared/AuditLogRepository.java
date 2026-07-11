package com.chaobo.scm.supplier.application.shared;

public interface AuditLogRepository {
    void save(CommandContext context, String operationType, String targetType, long targetId,
              String targetNo, String beforeSnapshot, String afterSnapshot);
}
