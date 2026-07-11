package com.chaobo.scm.purchase.application.comparison;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.application.price.PurchasePriceApplicationService;
import com.chaobo.scm.purchase.application.price.PurchasePriceCommands;
import com.chaobo.scm.purchase.application.shared.AuditLogRepository;
import com.chaobo.scm.purchase.application.shared.CommandContext;
import com.chaobo.scm.purchase.application.shared.CommandResult;
import com.chaobo.scm.purchase.application.shared.IdempotencyPort;
import com.chaobo.scm.purchase.application.shared.OutboxRepository;
import com.chaobo.scm.purchase.domain.comparison.BidCandidate;
import com.chaobo.scm.purchase.domain.comparison.BidComparisonAggregate;
import com.chaobo.scm.purchase.domain.comparison.BidComparisonRepository;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BidComparisonApplicationService {
    private final BidComparisonRepository repository;
    private final OutboxRepository outbox;
    private final AuditLogRepository auditLog;
    private final IdentifierGenerator ids;
    private final IdempotencyPort idempotency;
    private final PurchasePriceApplicationService purchasePrices;

    public BidComparisonApplicationService(
            BidComparisonRepository repository,
            OutboxRepository outbox,
            AuditLogRepository auditLog,
            IdentifierGenerator ids,
            IdempotencyPort idempotency,
            PurchasePriceApplicationService purchasePrices) {
        this.repository = repository;
        this.outbox = outbox;
        this.auditLog = auditLog;
        this.ids = ids;
        this.idempotency = idempotency;
        this.purchasePrices = purchasePrices;
    }

    @Transactional
    public CommandResult generate(BidComparisonCommands.Generate command, CommandContext context) {
        context.requirePermission("purchase:comparison:generate");
        context.requirePurchaseOrgScope(command.purchaseOrgId());
        return idempotency.execute("purchase:comparison:generate", context, () -> {
            var aggregate = BidComparisonAggregate.generate(
                    command.rfqNo(),
                    command.purchaseOrgId(),
                    command.currency(),
                    candidates(command.candidates()),
                    ids);
            return persist(aggregate, context, "GENERATE_COMPARISON", null);
        });
    }

    @Transactional
    public CommandResult award(String compareNo, BidComparisonCommands.Award command, CommandContext context) {
        context.requirePermission("purchase:comparison:award");
        return idempotency.execute("purchase:comparison:award", context, () -> {
            var aggregate = repository.findByNo(compareNo)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "比价结果不存在"));
            context.requirePurchaseOrgScope(aggregate.purchaseOrgId());
            if (aggregate.version() != command.version()) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "比价结果已被其他人修改");
            }
            var before = snapshot(aggregate);
            var winner = aggregate.award(command.candidateId(), command.reason(), context.operatorId(), ids);
            if (command.activatePurchasePrice()) {
                purchasePrices.create(new PurchasePriceCommands.Create(
                        winner.supplierId(),
                        winner.skuCode(),
                        aggregate.purchaseOrgId(),
                        command.priceType(),
                        aggregate.currency(),
                        winner.unitPrice(),
                        winner.taxRate(),
                        command.effectiveFrom(),
                        command.effectiveTo(),
                        "BID_COMPARISON",
                        aggregate.compareNo()), context);
            }
            return persist(aggregate, context, "AWARD_COMPARISON", before);
        });
    }

    private CommandResult persist(
            BidComparisonAggregate aggregate,
            CommandContext context,
            String operation,
            String before) {
        repository.save(aggregate, context.operatorId());
        var events = aggregate.pullEvents();
        outbox.saveAll(events);
        auditLog.save(context, operation, "BID_COMPARISON", aggregate.id(), aggregate.compareNo(), before, snapshot(aggregate));
        var eventCode = events.isEmpty() ? null : events.getLast().eventCode();
        return new CommandResult(
                aggregate.id(),
                aggregate.compareNo(),
                aggregate.status().code(),
                aggregate.status().label(),
                aggregate.version(),
                eventCode,
                false);
    }

    private List<BidCandidate> candidates(List<BidComparisonCommands.Candidate> commands) {
        if (commands == null || commands.size() < 2) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "生成比价至少需要两个候选报价");
        }
        return commands.stream()
                .map(candidate -> new BidCandidate(
                        candidate.candidateId() == null ? ids.nextId() : candidate.candidateId(),
                        candidate.supplierId(),
                        candidate.supplierName(),
                        candidate.quoteNo(),
                        candidate.skuCode(),
                        candidate.quoteQty(),
                        candidate.unitPrice(),
                        candidate.taxRate(),
                        candidate.deliveryDays(),
                        candidate.supplierScore(),
                        candidate.transportScore(),
                        candidate.estimatedFreightCost(),
                        false))
                .toList();
    }

    private String snapshot(BidComparisonAggregate aggregate) {
        return "{\"compareNo\":\"%s\",\"status\":%d,\"version\":%d}"
                .formatted(aggregate.compareNo(), aggregate.status().code(), aggregate.version());
    }
}
