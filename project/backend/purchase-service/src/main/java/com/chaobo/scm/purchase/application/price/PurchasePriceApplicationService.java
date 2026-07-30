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

/**
 * PurchasePriceApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class PurchasePriceApplicationService {

    /**
     * repository（类型：{@code PurchasePriceRepository}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final PurchasePriceRepository repository;

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
     * 创建 PurchasePriceApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param repository 持久化访问依赖，类型为 {@code PurchasePriceRepository}
     * @param outbox 业务处理参数或成员，类型为 {@code OutboxRepository}
     * @param auditLog 业务处理参数或成员，类型为 {@code AuditLogRepository}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param idempotency 业务或技术标识，类型为 {@code IdempotencyPort}
     */
    public PurchasePriceApplicationService(PurchasePriceRepository repository, OutboxRepository outbox, AuditLogRepository auditLog, IdentifierGenerator ids, IdempotencyPort idempotency) {
        this.repository = repository;
        this.outbox = outbox;
        this.auditLog = auditLog;
        this.ids = ids;
        this.idempotency = idempotency;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code PurchasePriceCommands.Create}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult create(PurchasePriceCommands.Create command, CommandContext context) {
        context.requirePermission("purchase:price:create");
        context.requirePurchaseOrgScope(command.purchaseOrgId());
        return idempotency.execute("purchase:price:create", context, () -> {
            var overlaps = repository.findActiveOverlaps(command.supplierId(), command.skuCode(), command.purchaseOrgId(), command.currency(), command.effectiveFrom(), command.effectiveTo());
            if (!overlaps.isEmpty()) {
                throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "存在有效期重叠的启用采购价格");
            }
            var aggregate = PurchasePriceAggregate.create(command.supplierId(), command.skuCode(), command.purchaseOrgId(), command.priceType(), command.currency(), command.unitPrice(), command.taxRate(), command.effectiveFrom(), command.effectiveTo(), command.sourceType(), command.sourceNo(), ids);
            return persist(aggregate, context, "CREATE_PURCHASE_PRICE", null);
        });
    }

    /**
     * 执行命令 {@code disable}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param priceNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code PurchasePriceCommands.Version}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult disable(String priceNo, PurchasePriceCommands.Version command, CommandContext context) {
        context.requirePermission("purchase:price:disable");
        return idempotency.execute("purchase:price:disable", context, () -> {
            var aggregate = repository.findByNo(priceNo).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "采购价格不存在"));
            context.requirePurchaseOrgScope(aggregate.purchaseOrgId());
            if (aggregate.version() != command.version()) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "采购价格已被其他人修改");
            }
            var before = snapshot(aggregate);
            aggregate.disable(ids);
            return persist(aggregate, context, "DISABLE_PURCHASE_PRICE", before);
        });
    }

    /**
     * 处理当前类型职责中的操作 {@code persist}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code PurchasePriceAggregate}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @param operation 业务处理参数或成员，类型为 {@code String}
     * @param before 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private CommandResult persist(PurchasePriceAggregate aggregate, CommandContext context, String operation, String before) {
        repository.save(aggregate, context.operatorId());
        var events = aggregate.pullEvents();
        outbox.saveAll(events);
        auditLog.save(context, operation, "PURCHASE_PRICE", aggregate.id(), aggregate.priceNo(), before, snapshot(aggregate));
        var eventCode = events.isEmpty() ? null : events.get(events.size() - 1).eventCode();
        return new CommandResult(aggregate.id(), aggregate.priceNo(), aggregate.status().code(), aggregate.status().label(), aggregate.version(), eventCode, false);
    }

    /**
     * 处理当前类型职责中的操作 {@code snapshot}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code PurchasePriceAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String snapshot(PurchasePriceAggregate aggregate) {
        return "{\"priceNo\":\"%s\",\"status\":%d,\"version\":%d}".formatted(aggregate.priceNo(), aggregate.status().code(), aggregate.version());
    }
}
