package com.chaobo.scm.supplier.infrastructure.persistence.event;

import com.chaobo.scm.supplier.application.shared.AuditLogRepository;
import com.chaobo.scm.supplier.application.shared.CommandContext;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisAuditLogRepository implements AuditLogRepository {
    private final EventPersistenceMapper mapper;
    private final IdentifierGenerator identifierGenerator;

    public MyBatisAuditLogRepository(EventPersistenceMapper mapper, IdentifierGenerator identifierGenerator) {
        this.mapper = mapper;
        this.identifierGenerator = identifierGenerator;
    }

    @Override
    public void save(CommandContext context, String operationType, String targetType, long targetId, String targetNo,
                     String beforeSnapshot, String afterSnapshot) {
        mapper.insertAudit(identifierGenerator.nextId(), context.operatorId(), context.operatorName(),
                operationType, targetType, targetId, targetNo, beforeSnapshot, afterSnapshot, context.requestId());
    }
}
