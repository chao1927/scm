package com.chaobo.scm.purchase.application.price;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.application.shared.AuditLogRepository;
import com.chaobo.scm.purchase.application.shared.CommandContext;
import com.chaobo.scm.purchase.application.shared.CommandResult;
import com.chaobo.scm.purchase.application.shared.IdempotencyPort;
import com.chaobo.scm.purchase.application.shared.OutboxRepository;
import com.chaobo.scm.purchase.domain.price.PurchasePriceAggregate;
import com.chaobo.scm.purchase.domain.price.PurchasePriceRepository;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchasePriceApplicationService {
    private final PurchasePriceRepository repository;
    private final OutboxRepository outbox;
    private final AuditLogRepository auditLog;
    private final IdentifierGenerator ids;
    private final IdempotencyPort idempotency;

    public PurchasePriceApplicationService(
            PurchasePriceRepository repository,
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
    public CommandResult create(PurchasePriceCommands.Create command, CommandContext context) {
        context.requirePermission("purchase:price:create");
        context.requirePurchaseOrgScope(command.purchaseOrgId());
        return idempotency.execute("purchase:price:create", context, () -> {
            var overlaps = repository.findActiveOverlaps(
                    command.supplierId(),
                    command.skuCode(),
                    command.purchaseOrgId(),
                    command.currency(),
                    command.effectiveFrom(),
                    command.effectiveTo());
            if (!overlaps.isEmpty()) {
                throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "存在有效期重叠的启用采购价格");
            }
            var aggregate = PurchasePriceAggregate.create(
                    command.supplierId(),
                    command.skuCode(),
                    command.purchaseOrgId(),
                    command.priceType(),
                    command.currency(),
                    command.unitPrice(),
                    command.taxRate(),
                    command.effectiveFrom(),
                    command.effectiveTo(),
                    command.sourceType(),
                    command.sourceNo(),
                    ids);
            return persist(aggregate, context, "CREATE_PURCHASE_PRICE", null);
        });
    }

    @Transactional
    public CommandResult disable(String priceNo, PurchasePriceCommands.Version command, CommandContext context) {
        context.requirePermission("purchase:price:disable");
        return idempotency.execute("purchase:price:disable", context, () -> {
            var aggregate = repository.findByNo(priceNo)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "采购价格不存在"));
            context.requirePurchaseOrgScope(aggregate.purchaseOrgId());
            if (aggregate.version() != command.version()) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "采购价格已被其他人修改");
            }
            var before = snapshot(aggregate);
            aggregate.disable(ids);
            return persist(aggregate, context, "DISABLE_PURCHASE_PRICE", before);
        });
    }

    private CommandResult persist(
            PurchasePriceAggregate aggregate,
            CommandContext context,
            String operation,
            String before) {
        repository.save(aggregate, context.operatorId());
        var events = aggregate.pullEvents();
        outbox.saveAll(events);
        auditLog.save(context, operation, "PURCHASE_PRICE", aggregate.id(), aggregate.priceNo(), before, snapshot(aggregate));
        var eventCode = events.isEmpty() ? null : events.getLast().eventCode();
        return new CommandResult(
                aggregate.id(),
                aggregate.priceNo(),
                aggregate.status().code(),
                aggregate.status().label(),
                aggregate.version(),
                eventCode,
                false);
    }

    private String snapshot(PurchasePriceAggregate aggregate) {
        return "{\"priceNo\":\"%s\",\"status\":%d,\"version\":%d}"
                .formatted(aggregate.priceNo(), aggregate.status().code(), aggregate.version());
    }
}
