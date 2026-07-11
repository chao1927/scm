package com.chaobo.scm.purchase.application.inbound;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.application.shared.*;
import com.chaobo.scm.purchase.domain.inbound.*;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InboundTrackingApplicationService {
    private final InboundTrackingRepository repository;
    private final OutboxRepository outbox;
    private final AuditLogRepository auditLog;
    private final IdentifierGenerator ids;
    private final IdempotencyPort idempotency;

    public InboundTrackingApplicationService(InboundTrackingRepository repository, OutboxRepository outbox,
                                             AuditLogRepository auditLog, IdentifierGenerator ids,
                                             IdempotencyPort idempotency) {
        this.repository = repository;
        this.outbox = outbox;
        this.auditLog = auditLog;
        this.ids = ids;
        this.idempotency = idempotency;
    }

    @Transactional
    public CommandResult recordAsn(InboundCommands.RecordAsn command, CommandContext context) {
        context.requirePermission("purchase:inbound:record-asn");
        context.requirePurchaseOrgScope(command.purchaseOrgId());
        return idempotency.execute("purchase:inbound:record-asn", context, () -> {
            repository.findByAsnNo(command.asnNo()).ifPresent(existing -> {
                throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "ASN已存在到货跟踪");
            });
            var aggregate = InboundTrackingAggregate.recordAsn(command.orderNo(), command.asnNo(), command.supplierId(),
                    command.purchaseOrgId(), command.warehouseCode(), command.skuCode(), command.notifiedQty(), ids);
            return persist(aggregate, context, "RECORD_ASN", null);
        });
    }

    @Transactional
    public CommandResult syncWms(String inboundNo, InboundCommands.SyncWms command, CommandContext context) {
        context.requirePermission("purchase:inbound:sync-wms");
        return idempotency.execute("purchase:inbound:sync-wms", context, () -> {
            var aggregate = repository.findByNo(inboundNo)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "到货跟踪不存在"));
            context.requirePurchaseOrgScope(aggregate.purchaseOrgId());
            if (aggregate.version() != command.version()) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "到货跟踪已被其他人修改");
            }
            var before = snapshot(aggregate);
            aggregate.syncWms(command.receivedQty(), command.qualifiedQty(), command.unqualifiedQty(),
                    command.putawayQty(), command.reason(), ids);
            return persist(aggregate, context, "SYNC_WMS_INBOUND", before);
        });
    }

    private CommandResult persist(InboundTrackingAggregate aggregate, CommandContext context, String operation,
                                  String before) {
        repository.save(aggregate, context.operatorId());
        var events = aggregate.pullEvents();
        outbox.saveAll(events);
        auditLog.save(context, operation, "INBOUND_TRACKING", aggregate.id(), aggregate.inboundNo(), before,
                snapshot(aggregate));
        var eventCode = events.isEmpty() ? null : events.getLast().eventCode();
        return new CommandResult(aggregate.id(), aggregate.inboundNo(), aggregate.status().code(),
                aggregate.status().label(), aggregate.version(), eventCode, false);
    }

    private String snapshot(InboundTrackingAggregate aggregate) {
        return "{\"inboundNo\":\"%s\",\"status\":%d,\"version\":%d}"
                .formatted(aggregate.inboundNo(), aggregate.status().code(), aggregate.version());
    }
}
