package com.chaobo.scm.purchase.infrastructure.persistence.event;

import com.chaobo.scm.purchase.application.shared.AuditLogRepository;
import com.chaobo.scm.purchase.application.shared.CommandContext;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisAuditLogRepository implements AuditLogRepository {
    private final EventPersistenceMapper mapper;

    public MyBatisAuditLogRepository(EventPersistenceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(
            CommandContext context,
            String operation,
            String targetType,
            long targetId,
            String targetNo,
            String beforeSnapshot,
            String afterSnapshot) {
        mapper.insertAuditLog(
                context.requestId(),
                context.traceId(),
                context.operatorId(),
                context.operatorName(),
                operation,
                targetType,
                targetId,
                targetNo,
                beforeSnapshot,
                afterSnapshot);
    }
}
