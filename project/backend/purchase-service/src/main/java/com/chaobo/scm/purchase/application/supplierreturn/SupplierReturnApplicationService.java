package com.chaobo.scm.purchase.application.supplierreturn;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.application.integration.IntegrationCommandEnqueuer;
import com.chaobo.scm.purchase.application.shared.*;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import com.chaobo.scm.purchase.domain.supplierreturn.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class SupplierReturnApplicationService {
    private final SupplierReturnRepository repository;
    private final OutboxRepository outbox;
    private final AuditLogRepository auditLog;
    private final IdentifierGenerator ids;
    private final IdempotencyPort idempotency;
    private final IntegrationCommandEnqueuer integrations;

    public SupplierReturnApplicationService(SupplierReturnRepository repository, OutboxRepository outbox,
                                            AuditLogRepository auditLog, IdentifierGenerator ids,
                                            IdempotencyPort idempotency, IntegrationCommandEnqueuer integrations) {
        this.repository = repository;
        this.outbox = outbox;
        this.auditLog = auditLog;
        this.ids = ids;
        this.idempotency = idempotency;
        this.integrations = integrations;
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
        return idempotency.execute("purchase:supplier-return:NOTIFY_SUPPLIER_RETURN", context, () -> {
            var aggregate = repository.findByNo(returnNo)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "退供申请不存在"));
            context.requirePurchaseOrgScope(aggregate.purchaseOrgId());
            if (aggregate.version() != command.version()) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "退供申请已被其他人修改");
            }
            var before = snapshot(aggregate);
            aggregate.notifyExecution(command.notifyMode(), ids);
            var result = persist(aggregate, context, "NOTIFY_SUPPLIER_RETURN", before);
            enqueueExecutionCommands(aggregate);
            return result;
        });
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

    private void enqueueExecutionCommands(SupplierReturnAggregate aggregate) {
        var payload = Map.of(
                "returnId", aggregate.id(),
                "returnNo", aggregate.returnNo(),
                "sourceOrderNo", aggregate.sourceOrderNo(),
                "supplierId", aggregate.supplierId(),
                "purchaseOrgId", aggregate.purchaseOrgId(),
                "warehouseCode", aggregate.warehouseCode() == null ? "" : aggregate.warehouseCode(),
                "version", aggregate.version(),
                "lines", aggregate.lines().stream().map(line -> Map.of(
                        "lineId", line.lineId(),
                        "skuCode", line.skuCode(),
                        "returnQty", line.returnQty(),
                        "returnableQty", line.returnableQty(),
                        "reason", line.reason() == null ? "" : line.reason()
                )).toList());
        integrations.enqueue("INVENTORY_LOCK_SUPPLIER_RETURN", "INVENTORY", "SUPPLIER_RETURN",
                Long.toString(aggregate.id()), aggregate.returnNo(), payload);
        integrations.enqueue("WMS_CREATE_SUPPLIER_RETURN_OUTBOUND", "WMS", "SUPPLIER_RETURN",
                Long.toString(aggregate.id()), aggregate.returnNo(), payload);
        integrations.enqueue("TMS_CREATE_SUPPLIER_RETURN_TRANSPORT", "TMS", "SUPPLIER_RETURN",
                Long.toString(aggregate.id()), aggregate.returnNo(), payload);
        integrations.enqueue("BMS_CREATE_SUPPLIER_RETURN_OFFSET", "BMS", "SUPPLIER_RETURN",
                Long.toString(aggregate.id()), aggregate.returnNo(), payload);
    }
}
