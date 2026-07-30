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

/**
 * BidComparisonApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class BidComparisonApplicationService {

    /**
     * repository（类型：{@code BidComparisonRepository}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final BidComparisonRepository repository;

    /**
     * outbox（类型：{@code OutboxRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final OutboxRepository outbox;

    /**
     * auditLog（类型：{@code AuditLogRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AuditLogRepository auditLog;

    /**
     * ids（类型：{@code IdentifierGenerator}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final IdentifierGenerator ids;

    /**
     * idempotency（类型：{@code IdempotencyPort}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final IdempotencyPort idempotency;

    /**
     * purchasePrices（类型：{@code PurchasePriceApplicationService}）。
     *
     * <p>保存当前对象所需的金额或计费值；其具体生命周期由所属对象统一管理。
     */
    private final PurchasePriceApplicationService purchasePrices;

    /**
     * 创建 BidComparisonApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param repository 持久化访问依赖，类型为 {@code BidComparisonRepository}
     * @param outbox 业务处理参数或成员，类型为 {@code OutboxRepository}
     * @param auditLog 业务处理参数或成员，类型为 {@code AuditLogRepository}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param idempotency 业务或技术标识，类型为 {@code IdempotencyPort}
     * @param purchasePrices 金额或计费值，类型为 {@code PurchasePriceApplicationService}
     */
    public BidComparisonApplicationService(BidComparisonRepository repository, OutboxRepository outbox, AuditLogRepository auditLog, IdentifierGenerator ids, IdempotencyPort idempotency, PurchasePriceApplicationService purchasePrices) {
        this.repository = repository;
        this.outbox = outbox;
        this.auditLog = auditLog;
        this.ids = ids;
        this.idempotency = idempotency;
        this.purchasePrices = purchasePrices;
    }

    /**
     * 处理当前类型职责中的操作 {@code generate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code BidComparisonCommands.Generate}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult generate(BidComparisonCommands.Generate command, CommandContext context) {
        context.requirePermission("purchase:comparison:generate");
        context.requirePurchaseOrgScope(command.purchaseOrgId());
        return idempotency.execute("purchase:comparison:generate", context, () -> {
            var aggregate = BidComparisonAggregate.generate(command.rfqNo(), command.purchaseOrgId(), command.currency(), candidates(command.candidates()), ids);
            return persist(aggregate, context, "GENERATE_COMPARISON", null);
        });
    }

    /**
     * 处理当前类型职责中的操作 {@code award}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param compareNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code BidComparisonCommands.Award}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult award(String compareNo, BidComparisonCommands.Award command, CommandContext context) {
        context.requirePermission("purchase:comparison:award");
        return idempotency.execute("purchase:comparison:award", context, () -> {
            var aggregate = repository.findByNo(compareNo).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "比价结果不存在"));
            context.requirePurchaseOrgScope(aggregate.purchaseOrgId());
            if (aggregate.version() != command.version()) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "比价结果已被其他人修改");
            }
            var before = snapshot(aggregate);
            var winner = aggregate.award(command.candidateId(), command.reason(), context.operatorId(), ids);
            if (command.activatePurchasePrice()) {
                purchasePrices.create(new PurchasePriceCommands.Create(winner.supplierId(), winner.skuCode(), aggregate.purchaseOrgId(), command.priceType(), aggregate.currency(), winner.unitPrice(), winner.taxRate(), command.effectiveFrom(), command.effectiveTo(), "BID_COMPARISON", aggregate.compareNo()), context);
            }
            return persist(aggregate, context, "AWARD_COMPARISON", before);
        });
    }

    /**
     * 处理当前类型职责中的操作 {@code persist}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code BidComparisonAggregate}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @param operation 业务处理参数或成员，类型为 {@code String}
     * @param before 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private CommandResult persist(BidComparisonAggregate aggregate, CommandContext context, String operation, String before) {
        repository.save(aggregate, context.operatorId());
        var events = aggregate.pullEvents();
        outbox.saveAll(events);
        auditLog.save(context, operation, "BID_COMPARISON", aggregate.id(), aggregate.compareNo(), before, snapshot(aggregate));
        var eventCode = events.isEmpty() ? null : events.get(events.size() - 1).eventCode();
        return new CommandResult(aggregate.id(), aggregate.compareNo(), aggregate.status().code(), aggregate.status().label(), aggregate.version(), eventCode, false);
    }

    /**
     * 处理当前类型职责中的操作 {@code candidates}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param commands 用例输入命令，类型为 {@code List<BidComparisonCommands.Candidate>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<BidCandidate>}
     */
    private List<BidCandidate> candidates(List<BidComparisonCommands.Candidate> commands) {
        if (commands == null || commands.size() < CANDIDATES_VALUE_2) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "生成比价至少需要两个候选报价");
        }
        return commands.stream().map(candidate -> new BidCandidate(candidate.candidateId() == null ? ids.nextId() : candidate.candidateId(), candidate.supplierId(), candidate.supplierName(), candidate.quoteNo(), candidate.skuCode(), candidate.quoteQty(), candidate.unitPrice(), candidate.taxRate(), candidate.deliveryDays(), candidate.supplierScore(), candidate.transportScore(), candidate.estimatedFreightCost(), false)).toList();
    }

    /**
     * 处理当前类型职责中的操作 {@code snapshot}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code BidComparisonAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String snapshot(BidComparisonAggregate aggregate) {
        return "{\"compareNo\":\"%s\",\"status\":%d,\"version\":%d}".formatted(aggregate.compareNo(), aggregate.status().code(), aggregate.version());
    }

    /**
     * 业务常量 {@code CANDIDATES_VALUE_2}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int CANDIDATES_VALUE_2 = 2;
}
