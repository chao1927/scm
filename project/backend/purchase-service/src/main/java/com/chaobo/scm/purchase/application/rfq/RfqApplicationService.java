package com.chaobo.scm.purchase.application.rfq;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.application.shared.AuditLogRepository;
import com.chaobo.scm.purchase.application.shared.CommandContext;
import com.chaobo.scm.purchase.application.shared.CommandResult;
import com.chaobo.scm.purchase.application.shared.IdempotencyPort;
import com.chaobo.scm.purchase.application.shared.OutboxRepository;
import com.chaobo.scm.purchase.domain.rfq.RfqAggregate;
import com.chaobo.scm.purchase.domain.rfq.RfqInvitation;
import com.chaobo.scm.purchase.domain.rfq.RfqLine;
import com.chaobo.scm.purchase.domain.rfq.RfqRepository;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * RfqApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class RfqApplicationService {

    /**
     * repository（类型：{@code RfqRepository}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final RfqRepository repository;

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
     * 创建 RfqApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param repository 持久化访问依赖，类型为 {@code RfqRepository}
     * @param outbox 业务处理参数或成员，类型为 {@code OutboxRepository}
     * @param auditLog 业务处理参数或成员，类型为 {@code AuditLogRepository}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param idempotency 业务或技术标识，类型为 {@code IdempotencyPort}
     */
    public RfqApplicationService(RfqRepository repository, OutboxRepository outbox, AuditLogRepository auditLog, IdentifierGenerator ids, IdempotencyPort idempotency) {
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
     * @param command 用例输入命令，类型为 {@code RfqCommands.Create}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult create(RfqCommands.Create command, CommandContext context) {
        context.requirePermission("purchase:rfq:create");
        context.requirePurchaseOrgScope(command.purchaseOrgId());
        return idempotency.execute("purchase:rfq:create", context, () -> {
            var aggregate = RfqAggregate.create(command.rfqType(), command.purchaseOrgId(), command.categoryCode(), command.sourceRequisitionNo(), command.quoteDeadline(), lines(command.lines()), invitations(command.invitedSupplierIds()), ids);
            return persist(aggregate, context, "CREATE_RFQ", null);
        });
    }

    /**
     * 执行命令 {@code publish}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param rfqNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code RfqCommands.Version}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult publish(String rfqNo, RfqCommands.Version command, CommandContext context) {
        context.requirePermission("purchase:rfq:publish");
        return change(rfqNo, command.version(), context, "PUBLISH_RFQ", aggregate -> aggregate.publish(ids));
    }

    /**
     * 执行命令 {@code close}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param rfqNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code RfqCommands.Close}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult close(String rfqNo, RfqCommands.Close command, CommandContext context) {
        context.requirePermission("purchase:rfq:close");
        return change(rfqNo, command.version(), context, "CLOSE_RFQ", aggregate -> aggregate.closeBidding(command.reason(), ids));
    }

    /**
     * 处理当前类型职责中的操作 {@code change}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param rfqNo 可追踪业务编码，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @param operation 业务处理参数或成员，类型为 {@code String}
     * @param action 业务处理参数或成员，类型为 {@code java.util.function.Consumer<RfqAggregate>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private CommandResult change(String rfqNo, int version, CommandContext context, String operation, java.util.function.Consumer<RfqAggregate> action) {
        return idempotency.execute("purchase:rfq:" + operation, context, () -> {
            var aggregate = repository.findByNo(rfqNo).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "询价单不存在"));
            context.requirePurchaseOrgScope(aggregate.purchaseOrgId());
            if (aggregate.version() != version) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "询价单已被其他人修改");
            }
            var before = snapshot(aggregate);
            action.accept(aggregate);
            return persist(aggregate, context, operation, before);
        });
    }

    /**
     * 处理当前类型职责中的操作 {@code persist}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code RfqAggregate}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @param operation 业务处理参数或成员，类型为 {@code String}
     * @param before 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private CommandResult persist(RfqAggregate aggregate, CommandContext context, String operation, String before) {
        repository.save(aggregate, context.operatorId());
        var events = aggregate.pullEvents();
        outbox.saveAll(events);
        auditLog.save(context, operation, "RFQ", aggregate.id(), aggregate.rfqNo(), before, snapshot(aggregate));
        var eventCode = events.isEmpty() ? null : events.get(events.size() - 1).eventCode();
        return new CommandResult(aggregate.id(), aggregate.rfqNo(), aggregate.status().code(), aggregate.status().label(), aggregate.version(), eventCode, false);
    }

    /**
     * 处理当前类型职责中的操作 {@code lines}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param commands 用例输入命令，类型为 {@code List<RfqCommands.Line>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<RfqLine>}
     */
    private List<RfqLine> lines(List<RfqCommands.Line> commands) {
        if (commands == null || commands.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "询价行不能为空");
        }
        return commands.stream().map(line -> new RfqLine(line.lineId() == null ? ids.nextId() : line.lineId(), line.skuCode(), line.targetQty(), line.uom(), line.requiredDeliveryDate(), line.qualityRequirement())).toList();
    }

    /**
     * 处理当前类型职责中的操作 {@code invitations}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param supplierIds 业务或技术标识，类型为 {@code List<Long>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<RfqInvitation>}
     */
    private List<RfqInvitation> invitations(List<Long> supplierIds) {
        if (supplierIds == null || supplierIds.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "邀请供应商不能为空");
        }
        return supplierIds.stream().map(supplierId -> new RfqInvitation(ids.nextId(), supplierId, 1)).toList();
    }

    /**
     * 处理当前类型职责中的操作 {@code snapshot}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code RfqAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String snapshot(RfqAggregate aggregate) {
        return "{\"rfqNo\":\"%s\",\"status\":%d,\"version\":%d}".formatted(aggregate.rfqNo(), aggregate.status().code(), aggregate.version());
    }
}
