package com.chaobo.scm.purchase.application.orderchange;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.application.order.PurchaseOrderApplicationService;
import com.chaobo.scm.purchase.application.shared.*;
import com.chaobo.scm.purchase.domain.orderchange.*;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PurchaseOrderChangeApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class PurchaseOrderChangeApplicationService {

    /**
     * repository（类型：{@code PurchaseOrderChangeRepository}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final PurchaseOrderChangeRepository repository;

    /**
     * orders（类型：{@code PurchaseOrderApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final PurchaseOrderApplicationService orders;

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
     * 创建 PurchaseOrderChangeApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param repository 持久化访问依赖，类型为 {@code PurchaseOrderChangeRepository}
     * @param orders 业务处理参数或成员，类型为 {@code PurchaseOrderApplicationService}
     * @param outbox 业务处理参数或成员，类型为 {@code OutboxRepository}
     * @param auditLog 业务处理参数或成员，类型为 {@code AuditLogRepository}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param idempotency 业务或技术标识，类型为 {@code IdempotencyPort}
     */
    public PurchaseOrderChangeApplicationService(PurchaseOrderChangeRepository repository, PurchaseOrderApplicationService orders, OutboxRepository outbox, AuditLogRepository auditLog, IdentifierGenerator ids, IdempotencyPort idempotency) {
        this.repository = repository;
        this.orders = orders;
        this.outbox = outbox;
        this.auditLog = auditLog;
        this.ids = ids;
        this.idempotency = idempotency;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code PurchaseOrderChangeCommands.Create}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult create(PurchaseOrderChangeCommands.Create command, CommandContext context) {
        context.requirePermission("purchase:po-change:create");
        return idempotency.execute("purchase:po-change:create", context, () -> persist(PurchaseOrderChangeAggregate.create(command.orderNo(), command.changeType(), command.beforeSnapshot(), command.afterSnapshot(), command.changeReason(), ids), context, "CREATE_PO_CHANGE", null));
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param changeNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code PurchaseOrderChangeCommands.Approve}
     * @param lineQtyChanges 数量值，类型为 {@code java.util.Map<Long,java.math.BigDecimal>}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult approve(String changeNo, PurchaseOrderChangeCommands.Approve command, java.util.Map<Long, java.math.BigDecimal> lineQtyChanges, CommandContext context) {
        context.requirePermission("purchase:po-change:approve");
        return idempotency.execute("purchase:po-change:approve", context, () -> {
            var change = repository.findByNo(changeNo).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "采购订单变更单不存在"));
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

    /**
     * 处理当前类型职责中的操作 {@code persist}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param change 业务处理参数或成员，类型为 {@code PurchaseOrderChangeAggregate}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @param operation 业务处理参数或成员，类型为 {@code String}
     * @param before 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private CommandResult persist(PurchaseOrderChangeAggregate change, CommandContext context, String operation, String before) {
        repository.save(change, context.operatorId());
        var events = change.pullEvents();
        outbox.saveAll(events);
        auditLog.save(context, operation, "PURCHASE_ORDER_CHANGE", change.id(), change.changeNo(), before, snapshot(change));
        var eventCode = events.isEmpty() ? null : events.get(events.size() - 1).eventCode();
        return new CommandResult(change.id(), change.changeNo(), change.status().code(), change.status().label(), change.version(), eventCode, false);
    }

    /**
     * 处理当前类型职责中的操作 {@code snapshot}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param change 业务处理参数或成员，类型为 {@code PurchaseOrderChangeAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String snapshot(PurchaseOrderChangeAggregate change) {
        return "{\"changeNo\":\"%s\",\"status\":%d,\"version\":%d}".formatted(change.changeNo(), change.status().code(), change.version());
    }
}
