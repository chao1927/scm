package com.chaobo.scm.purchase.application.rfq;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.application.shared.AuditLogRepository;
import com.chaobo.scm.purchase.application.shared.CommandContext;
import com.chaobo.scm.purchase.application.shared.CommandResult;
import com.chaobo.scm.purchase.application.shared.IdempotencyPort;
import com.chaobo.scm.purchase.application.shared.OutboxRepository;
import com.chaobo.scm.purchase.domain.rfq.RfqAggregate;
import com.chaobo.scm.purchase.domain.rfq.RfqInvitation;
import com.chaobo.scm.purchase.domain.rfq.RfqLine;
import com.chaobo.scm.purchase.domain.rfq.RfqRepository;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RfqApplicationService {
    private final RfqRepository repository;
    private final OutboxRepository outbox;
    private final AuditLogRepository auditLog;
    private final IdentifierGenerator ids;
    private final IdempotencyPort idempotency;

    public RfqApplicationService(
            RfqRepository repository,
            OutboxRepository outbox,
            AuditLogRepository auditLog,
            IdentifierGenerator ids,
            IdempotencyPort idempotency) {
        this.repository = repository;
        this.outbox = outbox;
        this.auditLog = auditLog;
        this.ids = ids;
        this.idempotency = idempotency;
    }

    @Transactional
    public CommandResult create(RfqCommands.Create command, CommandContext context) {
        context.requirePermission("purchase:rfq:create");
        context.requirePurchaseOrgScope(command.purchaseOrgId());
        return idempotency.execute("purchase:rfq:create", context, () -> {
            var aggregate = RfqAggregate.create(
                    command.rfqType(),
                    command.purchaseOrgId(),
                    command.categoryCode(),
                    command.sourceRequisitionNo(),
                    command.quoteDeadline(),
                    lines(command.lines()),
                    invitations(command.invitedSupplierIds()),
                    ids);
            return persist(aggregate, context, "CREATE_RFQ", null);
        });
    }

    @Transactional
    public CommandResult publish(String rfqNo, RfqCommands.Version command, CommandContext context) {
        context.requirePermission("purchase:rfq:publish");
        return change(rfqNo, command.version(), context, "PUBLISH_RFQ", aggregate -> aggregate.publish(ids));
    }

    @Transactional
    public CommandResult close(String rfqNo, RfqCommands.Close command, CommandContext context) {
        context.requirePermission("purchase:rfq:close");
        return change(rfqNo, command.version(), context, "CLOSE_RFQ",
                aggregate -> aggregate.closeBidding(command.reason(), ids));
    }

    private CommandResult change(
            String rfqNo,
            int version,
            CommandContext context,
            String operation,
            java.util.function.Consumer<RfqAggregate> action) {
        return idempotency.execute("purchase:rfq:" + operation, context, () -> {
            var aggregate = repository.findByNo(rfqNo)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "询价单不存在"));
            context.requirePurchaseOrgScope(aggregate.purchaseOrgId());
            if (aggregate.version() != version) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "询价单已被其他人修改");
            }
            var before = snapshot(aggregate);
            action.accept(aggregate);
            return persist(aggregate, context, operation, before);
        });
    }

    private CommandResult persist(RfqAggregate aggregate, CommandContext context, String operation, String before) {
        repository.save(aggregate, context.operatorId());
        var events = aggregate.pullEvents();
        outbox.saveAll(events);
        auditLog.save(context, operation, "RFQ", aggregate.id(), aggregate.rfqNo(), before, snapshot(aggregate));
        var eventCode = events.isEmpty() ? null : events.getLast().eventCode();
        return new CommandResult(
                aggregate.id(),
                aggregate.rfqNo(),
                aggregate.status().code(),
                aggregate.status().label(),
                aggregate.version(),
                eventCode,
                false);
    }

    private List<RfqLine> lines(List<RfqCommands.Line> commands) {
        if (commands == null || commands.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "询价行不能为空");
        }
        return commands.stream()
                .map(line -> new RfqLine(
                        line.lineId() == null ? ids.nextId() : line.lineId(),
                        line.skuCode(),
                        line.targetQty(),
                        line.uom(),
                        line.requiredDeliveryDate(),
                        line.qualityRequirement()))
                .toList();
    }

    private List<RfqInvitation> invitations(List<Long> supplierIds) {
        if (supplierIds == null || supplierIds.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "邀请供应商不能为空");
        }
        return supplierIds.stream()
                .map(supplierId -> new RfqInvitation(ids.nextId(), supplierId, 1))
                .toList();
    }

    private String snapshot(RfqAggregate aggregate) {
        return "{\"rfqNo\":\"%s\",\"status\":%d,\"version\":%d}"
                .formatted(aggregate.rfqNo(), aggregate.status().code(), aggregate.version());
    }
}
