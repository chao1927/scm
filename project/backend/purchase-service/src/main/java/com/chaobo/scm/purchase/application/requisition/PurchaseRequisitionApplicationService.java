package com.chaobo.scm.purchase.application.requisition;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.application.shared.AuditLogRepository;
import com.chaobo.scm.purchase.application.shared.CommandContext;
import com.chaobo.scm.purchase.application.shared.CommandResult;
import com.chaobo.scm.purchase.application.shared.IdempotencyPort;
import com.chaobo.scm.purchase.application.shared.OutboxRepository;
import com.chaobo.scm.purchase.domain.requisition.PurchaseRequisitionAggregate;
import com.chaobo.scm.purchase.domain.requisition.PurchaseRequisitionLine;
import com.chaobo.scm.purchase.domain.requisition.PurchaseRequisitionRepository;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class PurchaseRequisitionApplicationService {
    private final PurchaseRequisitionRepository repository;
    private final OutboxRepository outbox;
    private final AuditLogRepository auditLog;
    private final IdentifierGenerator ids;
    private final IdempotencyPort idempotency;

    public PurchaseRequisitionApplicationService(
            PurchaseRequisitionRepository repository,
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
    public CommandResult create(PurchaseRequisitionCommands.Save command, CommandContext context) {
        context.requirePermission("purchase:requisition:create");
        context.requirePurchaseOrgScope(command.purchaseOrgId());
        return idempotency.execute("purchase:requisition:create", context, () -> {
            var aggregate = PurchaseRequisitionAggregate.create(
                    command.applicantId(),
                    command.purchaseOrgId(),
                    command.demandDepartmentId(),
                    command.reason(),
                    lines(command.lines()),
                    ids);
            return persist(aggregate, context, "CREATE_REQUISITION", null);
        });
    }

    @Transactional
    public CommandResult update(long id, PurchaseRequisitionCommands.Save command, CommandContext context) {
        context.requirePermission("purchase:requisition:update");
        return idempotency.execute("purchase:requisition:update", context, () -> {
            var aggregate = load(id);
            context.requirePurchaseOrgScope(aggregate.purchaseOrgId());
            checkVersion(aggregate, command.version());
            var before = snapshot(aggregate);
            aggregate.changeDraft(command.reason(), lines(command.lines()), ids);
            return persist(aggregate, context, "UPDATE_REQUISITION", before);
        });
    }

    @Transactional
    public CommandResult submit(long id, int version, CommandContext context) {
        context.requirePermission("purchase:requisition:submit");
        return change(id, version, context, "SUBMIT_REQUISITION", aggregate -> aggregate.submit(ids));
    }

    @Transactional
    public CommandResult approve(long id, PurchaseRequisitionCommands.Approve command, CommandContext context) {
        context.requirePermission("purchase:requisition:approve");
        return change(id, command.version(), context, "APPROVE_REQUISITION",
                aggregate -> aggregate.approve(defaultMap(command.approvedQuantities()), ids));
    }

    @Transactional
    public CommandResult reject(long id, PurchaseRequisitionCommands.Reject command, CommandContext context) {
        context.requirePermission("purchase:requisition:approve");
        return change(id, command.version(), context, "REJECT_REQUISITION",
                aggregate -> aggregate.reject(command.reason(), ids));
    }

    @Transactional
    public CommandResult convert(long id, PurchaseRequisitionCommands.Convert command, CommandContext context) {
        context.requirePermission("purchase:requisition:convert");
        return change(id, command.version(), context, "CONVERT_REQUISITION",
                aggregate -> aggregate.convert(command.quantities(), command.targetType(), command.targetNo(), ids));
    }

    private CommandResult change(
            long id,
            int version,
            CommandContext context,
            String operation,
            java.util.function.Consumer<PurchaseRequisitionAggregate> action) {
        return idempotency.execute("purchase:requisition:" + operation, context, () -> {
            var aggregate = load(id);
            context.requirePurchaseOrgScope(aggregate.purchaseOrgId());
            checkVersion(aggregate, version);
            var before = snapshot(aggregate);
            action.accept(aggregate);
            return persist(aggregate, context, operation, before);
        });
    }

    private CommandResult persist(
            PurchaseRequisitionAggregate aggregate,
            CommandContext context,
            String operation,
            String before) {
        repository.save(aggregate, context.operatorId());
        var events = aggregate.pullEvents();
        outbox.saveAll(events);
        auditLog.save(
                context,
                operation,
                "PURCHASE_REQUISITION",
                aggregate.id(),
                aggregate.requisitionNo(),
                before,
                snapshot(aggregate));
        var eventCode = events.isEmpty() ? null : events.getLast().eventCode();
        return new CommandResult(
                aggregate.id(),
                aggregate.requisitionNo(),
                aggregate.status().code(),
                aggregate.status().label(),
                aggregate.version(),
                eventCode,
                false);
    }

    private PurchaseRequisitionAggregate load(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "请购单不存在"));
    }

    private void checkVersion(PurchaseRequisitionAggregate aggregate, int version) {
        if (aggregate.version() != version) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "请购单已被其他人修改");
        }
    }

    private List<PurchaseRequisitionLine> lines(List<PurchaseRequisitionCommands.Line> commands) {
        if (commands == null || commands.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "请购行不能为空");
        }
        return commands.stream()
                .map(line -> new PurchaseRequisitionLine(
                        line.lineId() == null ? ids.nextId() : line.lineId(),
                        line.skuCode(),
                        line.requestedQty(),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        line.purchaseUnit(),
                        line.requiredDate(),
                        line.remark()))
                .toList();
    }

    private static Map<Long, BigDecimal> defaultMap(Map<Long, BigDecimal> value) {
        return value == null ? Map.of() : value;
    }

    private String snapshot(PurchaseRequisitionAggregate aggregate) {
        return "{\"requisitionNo\":\"%s\",\"status\":%d,\"version\":%d}"
                .formatted(aggregate.requisitionNo(), aggregate.status().code(), aggregate.version());
    }
}
