package com.chaobo.scm.purchase.application.order;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.application.integration.IntegrationCommandEnqueuer;
import com.chaobo.scm.purchase.application.shared.*;
import com.chaobo.scm.purchase.domain.order.*;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class PurchaseOrderApplicationService {
    private final PurchaseOrderRepository repository;
    private final OutboxRepository outbox;
    private final AuditLogRepository auditLog;
    private final IdentifierGenerator ids;
    private final IdempotencyPort idempotency;
    private final IntegrationCommandEnqueuer integrations;

    public PurchaseOrderApplicationService(PurchaseOrderRepository repository, OutboxRepository outbox,
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
        return idempotency.execute("purchase:po:PUBLISH_PO", context, () -> {
            var order = load(orderNo);
            context.requirePurchaseOrgScope(order.purchaseOrgId());
            if (order.version() != command.version()) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "采购订单已被其他人修改");
            }
            var before = snapshot(order);
            order.publish(command.publishMode(), ids);
            var result = persist(order, context, "PUBLISH_PO", before);
            enqueuePublishCommands(order);
            return result;
        });
    }

    @Transactional
    public CommandResult cancel(String orderNo, PurchaseOrderCommands.Cancel command, CommandContext context) {
        context.requirePermission("purchase:po:cancel");
        return change(orderNo, command.version(), context, "CANCEL_PO",
                order -> order.cancel(command.reason(), ids));
    }

    @Transactional
    public CommandResult close(String orderNo, PurchaseOrderCommands.Close command, CommandContext context) {
        context.requirePermission("purchase:po:close");
        return change(orderNo, command.version(), context, "CLOSE_PO",
                order -> order.closeRemaining(command.reason(), ids));
    }

    /**
     * 处理供应商系统投递的确认、拒绝和差异事实。该入口只供本地 Inbox 消费器调用，
     * 消息幂等由 Inbox 保证，聚合版本由本地订单状态机维护。
     */
    @Transactional
    public CommandResult recordSupplierResponse(String orderNo, long supplierId, SupplierResponseType responseType,
                                                String reason, CommandContext context) {
        var order = load(orderNo);
        context.requirePurchaseOrgScope(order.purchaseOrgId());
        if (order.supplierId() != supplierId) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "供应商确认事件与采购订单供应商不一致");
        }
        var before = snapshot(order);
        switch (responseType) {
            case CONFIRMED -> order.recordSupplierConfirmation(reason, ids);
            case REJECTED -> order.recordSupplierRejection(reason, ids);
            case DIFFERENCE -> order.recordSupplierDifference(reason, ids);
        }
        return persist(order, context, "RECORD_SUPPLIER_" + responseType.name(), before);
    }

    @Transactional
    public CommandResult acceptSupplierDifference(String orderNo, String comment, CommandContext context) {
        return change(orderNo, null, context, "ACCEPT_SUPPLIER_DIFFERENCE",
                order -> order.acceptSupplierDifference(comment, ids));
    }

    @Transactional
    public CommandResult restartSupplierNegotiation(String orderNo, String requirement, CommandContext context) {
        return change(orderNo, null, context, "RESTART_SUPPLIER_NEGOTIATION",
                order -> order.restartSupplierNegotiation(requirement, ids));
    }

    @Transactional
    public CommandResult cancelFromSupplierResponse(String orderNo, String reason, CommandContext context) {
        return change(orderNo, null, context, "CANCEL_PO_FROM_SUPPLIER_RESPONSE",
                order -> order.cancel(reason, ids));
    }

    @Transactional
    public void applyChange(String orderNo, java.util.Map<Long, BigDecimal> lineQtyChanges, CommandContext context) {
        var order = load(orderNo);
        context.requirePurchaseOrgScope(order.purchaseOrgId());
        var before = snapshot(order);
        order.applyLineQtyChanges(lineQtyChanges, ids);
        persist(order, context, "APPLY_PO_CHANGE", before);
    }

    private CommandResult change(String orderNo, Integer version, CommandContext context, String operation,
                                 java.util.function.Consumer<PurchaseOrderAggregate> action) {
        return idempotency.execute("purchase:po:" + operation, context, () -> {
            var order = load(orderNo);
            context.requirePurchaseOrgScope(order.purchaseOrgId());
            if (version != null && order.version() != version) {
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

    private void enqueuePublishCommands(PurchaseOrderAggregate order) {
        var payload = Map.of(
                "orderId", order.id(),
                "orderNo", order.orderNo(),
                "supplierId", order.supplierId(),
                "purchaseOrgId", order.purchaseOrgId(),
                "warehouseCode", order.warehouseCode() == null ? "" : order.warehouseCode(),
                "currency", order.currency(),
                "version", order.version(),
                "lines", order.lines().stream().map(line -> Map.of(
                        "lineId", line.lineId(),
                        "skuCode", line.skuCode(),
                        "orderQty", line.orderQty(),
                        "unitPrice", line.unitPrice(),
                        "requiredDeliveryDate", line.requiredDeliveryDate() == null ? "" : line.requiredDeliveryDate().toString()
                )).toList());
        integrations.enqueue("SUPPLIER_CREATE_PO_CONFIRM_TODO", "SUPPLIER", "PURCHASE_ORDER",
                Long.toString(order.id()), order.orderNo(), payload);
        integrations.enqueue("WMS_CREATE_PURCHASE_INBOUND_PLAN", "WMS", "PURCHASE_ORDER",
                Long.toString(order.id()), order.orderNo(), payload);
        integrations.enqueue("BMS_CREATE_PURCHASE_PAYABLE_PLAN", "BMS", "PURCHASE_ORDER",
                Long.toString(order.id()), order.orderNo(), payload);
    }

    public enum SupplierResponseType {
        CONFIRMED,
        REJECTED,
        DIFFERENCE
    }
}
