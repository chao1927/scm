package com.chaobo.scm.purchase.application.order;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.application.shared.*;
import com.chaobo.scm.purchase.domain.order.*;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PurchaseOrderApplicationService {
    private final PurchaseOrderRepository repository;
    private final OutboxRepository outbox;
    private final AuditLogRepository auditLog;
    private final IdentifierGenerator ids;
    private final IdempotencyPort idempotency;

    public PurchaseOrderApplicationService(PurchaseOrderRepository repository, OutboxRepository outbox,
                                           AuditLogRepository auditLog, IdentifierGenerator ids,
                                           IdempotencyPort idempotency) {
        this.repository = repository;
        this.outbox = outbox;
        this.auditLog = auditLog;
        this.ids = ids;
        this.idempotency = idempotency;
    }

    @Transactional
    public CommandResult create(PurchaseOrderCommands.Create command, CommandContext context) {
        context.requirePermission("purchase:po:create");
        context.requirePurchaseOrgScope(command.purchaseOrgId());
        return idempotency.execute("purchase:po:create", context, () -> persist(PurchaseOrderAggregate.create(
                command.purchaseType(), command.supplierId(), command.supplierCode(), command.supplierName(),
                command.purchaseOrgId(), command.warehouseCode(), command.currency(), lines(command.lines()), ids),
                context, "CREATE_PO", null));
    }

    @Transactional
    public CommandResult submit(String orderNo, PurchaseOrderCommands.Version command, CommandContext context) {
        context.requirePermission("purchase:po:submit");
        return change(orderNo, command.version(), context, "SUBMIT_PO", order -> order.submit(ids));
    }

    @Transactional
    public CommandResult approve(String orderNo, PurchaseOrderCommands.Approve command, CommandContext context) {
        context.requirePermission("purchase:po:approve");
        return change(orderNo, command.version(), context, "APPROVE_PO",
                order -> order.approve(command.approved(), command.reason(), ids));
    }

    @Transactional
    public CommandResult publish(String orderNo, PurchaseOrderCommands.Publish command, CommandContext context) {
        context.requirePermission("purchase:po:publish");
        return change(orderNo, command.version(), context, "PUBLISH_PO",
                order -> order.publish(command.publishMode(), ids));
    }

    @Transactional
    public CommandResult cancel(String orderNo, PurchaseOrderCommands.Cancel command, CommandContext context) {
        context.requirePermission("purchase:po:cancel");
        return change(orderNo, command.version(), context, "CANCEL_PO",
                order -> order.cancel(command.reason(), ids));
    }

    @Transactional
    public void applyChange(String orderNo, java.util.Map<Long, BigDecimal> lineQtyChanges, CommandContext context) {
        var order = load(orderNo);
        context.requirePurchaseOrgScope(order.purchaseOrgId());
        var before = snapshot(order);
        order.applyLineQtyChanges(lineQtyChanges, ids);
        persist(order, context, "APPLY_PO_CHANGE", before);
    }

    private CommandResult change(String orderNo, int version, CommandContext context, String operation,
                                 java.util.function.Consumer<PurchaseOrderAggregate> action) {
        return idempotency.execute("purchase:po:" + operation, context, () -> {
            var order = load(orderNo);
            context.requirePurchaseOrgScope(order.purchaseOrgId());
            if (order.version() != version) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "采购订单已被其他人修改");
            }
            var before = snapshot(order);
            action.accept(order);
            return persist(order, context, operation, before);
        });
    }

    private PurchaseOrderAggregate load(String orderNo) {
        return repository.findByNo(orderNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "采购订单不存在"));
    }

    private CommandResult persist(PurchaseOrderAggregate order, CommandContext context, String operation, String before) {
        repository.save(order, context.operatorId());
        var events = order.pullEvents();
        outbox.saveAll(events);
        auditLog.save(context, operation, "PURCHASE_ORDER", order.id(), order.orderNo(), before, snapshot(order));
        var eventCode = events.isEmpty() ? null : events.getLast().eventCode();
        return new CommandResult(order.id(), order.orderNo(), order.status().code(), order.status().label(),
                order.version(), eventCode, false);
    }

    private List<PurchaseOrderLine> lines(List<PurchaseOrderCommands.Line> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "采购订单行不能为空");
        }
        return lines.stream().map(line -> new PurchaseOrderLine(line.lineId() == null ? ids.nextId() : line.lineId(),
                line.skuCode(), line.skuName(), line.orderQty(), line.unitPrice(), line.taxRate(), null,
                line.requiredDeliveryDate(), BigDecimal.ZERO)).toList();
    }

    private String snapshot(PurchaseOrderAggregate order) {
        return "{\"orderNo\":\"%s\",\"status\":%d,\"version\":%d}"
                .formatted(order.orderNo(), order.status().code(), order.version());
    }
}
