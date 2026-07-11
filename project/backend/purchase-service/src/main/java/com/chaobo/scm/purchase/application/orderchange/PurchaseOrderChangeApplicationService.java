package com.chaobo.scm.purchase.application.orderchange;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.application.order.PurchaseOrderApplicationService;
import com.chaobo.scm.purchase.application.shared.*;
import com.chaobo.scm.purchase.domain.orderchange.*;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseOrderChangeApplicationService {
    private final PurchaseOrderChangeRepository repository;
    private final PurchaseOrderApplicationService orders;
    private final OutboxRepository outbox;
    private final AuditLogRepository auditLog;
    private final IdentifierGenerator ids;
    private final IdempotencyPort idempotency;

    public PurchaseOrderChangeApplicationService(PurchaseOrderChangeRepository repository,
                                                 PurchaseOrderApplicationService orders, OutboxRepository outbox,
                                                 AuditLogRepository auditLog, IdentifierGenerator ids,
                                                 IdempotencyPort idempotency) {
        this.repository = repository;
        this.orders = orders;
        this.outbox = outbox;
        this.auditLog = auditLog;
        this.ids = ids;
        this.idempotency = idempotency;
    }

    @Transactional
    public CommandResult create(PurchaseOrderChangeCommands.Create command, CommandContext context) {
        context.requirePermission("purchase:po-change:create");
        return idempotency.execute("purchase:po-change:create", context, () -> persist(
                PurchaseOrderChangeAggregate.create(command.orderNo(), command.changeType(), command.beforeSnapshot(),
                        command.afterSnapshot(), command.changeReason(), ids), context, "CREATE_PO_CHANGE", null));
    }

    @Transactional
    public CommandResult approve(String changeNo, PurchaseOrderChangeCommands.Approve command,
                                 java.util.Map<Long, java.math.BigDecimal> lineQtyChanges, CommandContext context) {
        context.requirePermission("purchase:po-change:approve");
        return idempotency.execute("purchase:po-change:approve", context, () -> {
            var change = repository.findByNo(changeNo)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "采购订单变更单不存在"));
            if (change.version() != command.version()) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "采购订单变更单已被其他人修改");
            }
            var before = snapshot(change);
            change.approve(command.approved(), ids);
            if (command.approved() && change.changeType() == 1) {
                orders.applyChange(change.orderNo(), lineQtyChanges, context);
            }
            return persist(change, context, "APPROVE_PO_CHANGE", before);
        });
    }

    private CommandResult persist(PurchaseOrderChangeAggregate change, CommandContext context, String operation,
                                  String before) {
        repository.save(change, context.operatorId());
        var events = change.pullEvents();
        outbox.saveAll(events);
        auditLog.save(context, operation, "PURCHASE_ORDER_CHANGE", change.id(), change.changeNo(), before,
                snapshot(change));
        var eventCode = events.isEmpty() ? null : events.getLast().eventCode();
        return new CommandResult(change.id(), change.changeNo(), change.status().code(), change.status().label(),
                change.version(), eventCode, false);
    }

    private String snapshot(PurchaseOrderChangeAggregate change) {
        return "{\"changeNo\":\"%s\",\"status\":%d,\"version\":%d}"
                .formatted(change.changeNo(), change.status().code(), change.version());
    }
}
