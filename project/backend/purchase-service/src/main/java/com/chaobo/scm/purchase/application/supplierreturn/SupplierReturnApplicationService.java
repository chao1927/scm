package com.chaobo.scm.purchase.application.supplierreturn;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.application.shared.*;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import com.chaobo.scm.purchase.domain.supplierreturn.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupplierReturnApplicationService {
    private final SupplierReturnRepository repository;
    private final OutboxRepository outbox;
    private final AuditLogRepository auditLog;
    private final IdentifierGenerator ids;
    private final IdempotencyPort idempotency;

    public SupplierReturnApplicationService(SupplierReturnRepository repository, OutboxRepository outbox,
                                            AuditLogRepository auditLog, IdentifierGenerator ids,
                                            IdempotencyPort idempotency) {
        this.repository = repository;
        this.outbox = outbox;
        this.auditLog = auditLog;
        this.ids = ids;
        this.idempotency = idempotency;
    }

    @Transactional
    public CommandResult create(SupplierReturnCommands.Create command, CommandContext context) {
        context.requirePermission("purchase:supplier-return:create");
        context.requirePurchaseOrgScope(command.purchaseOrgId());
        return idempotency.execute("purchase:supplier-return:create", context, () -> persist(
                SupplierReturnAggregate.create(command.sourceOrderNo(), command.supplierId(), command.purchaseOrgId(),
                        command.warehouseCode(), lines(command.lines()), ids), context, "CREATE_SUPPLIER_RETURN", null));
    }

    @Transactional
    public CommandResult submit(String returnNo, SupplierReturnCommands.Version command, CommandContext context) {
        context.requirePermission("purchase:supplier-return:submit");
        return change(returnNo, command.version(), context, "SUBMIT_SUPPLIER_RETURN", aggregate -> aggregate.submit(ids));
    }

    @Transactional
    public CommandResult approve(String returnNo, SupplierReturnCommands.Approve command, CommandContext context) {
        context.requirePermission("purchase:supplier-return:approve");
        return change(returnNo, command.version(), context, "APPROVE_SUPPLIER_RETURN",
                aggregate -> aggregate.approve(command.approved(), command.reason(), ids));
    }

    @Transactional
    public CommandResult notifyExecution(String returnNo, SupplierReturnCommands.Notify command, CommandContext context) {
        context.requirePermission("purchase:supplier-return:notify");
        return change(returnNo, command.version(), context, "NOTIFY_SUPPLIER_RETURN",
                aggregate -> aggregate.notifyExecution(command.notifyMode(), ids));
    }

    private CommandResult change(String returnNo, int version, CommandContext context, String operation,
                                 java.util.function.Consumer<SupplierReturnAggregate> action) {
        return idempotency.execute("purchase:supplier-return:" + operation, context, () -> {
            var aggregate = repository.findByNo(returnNo)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "退供申请不存在"));
            context.requirePurchaseOrgScope(aggregate.purchaseOrgId());
            if (aggregate.version() != version) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "退供申请已被其他人修改");
            }
            var before = snapshot(aggregate);
            action.accept(aggregate);
            return persist(aggregate, context, operation, before);
        });
    }

    private CommandResult persist(SupplierReturnAggregate aggregate, CommandContext context, String operation,
                                  String before) {
        repository.save(aggregate, context.operatorId());
        var events = aggregate.pullEvents();
        outbox.saveAll(events);
        auditLog.save(context, operation, "SUPPLIER_RETURN", aggregate.id(), aggregate.returnNo(), before,
                snapshot(aggregate));
        var eventCode = events.isEmpty() ? null : events.getLast().eventCode();
        return new CommandResult(aggregate.id(), aggregate.returnNo(), aggregate.status().code(),
                aggregate.status().label(), aggregate.version(), eventCode, false);
    }

    private List<SupplierReturnLine> lines(List<SupplierReturnCommands.Line> commands) {
        if (commands == null || commands.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "退供行不能为空");
        }
        return commands.stream().map(line -> new SupplierReturnLine(line.lineId() == null ? ids.nextId() : line.lineId(),
                line.skuCode(), line.returnQty(), line.returnableQty(), line.reason())).toList();
    }

    private String snapshot(SupplierReturnAggregate aggregate) {
        return "{\"returnNo\":\"%s\",\"status\":%d,\"version\":%d}"
                .formatted(aggregate.returnNo(), aggregate.status().code(), aggregate.version());
    }
}
