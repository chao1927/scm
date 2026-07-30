package com.chaobo.scm.purchase.application.inbound;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.application.shared.*;
import com.chaobo.scm.purchase.domain.inbound.*;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * InboundTrackingApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class InboundTrackingApplicationService {

    /**
     * repository（类型：{@code InboundTrackingRepository}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final InboundTrackingRepository repository;

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
     * 创建 InboundTrackingApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param repository 持久化访问依赖，类型为 {@code InboundTrackingRepository}
     * @param outbox 业务处理参数或成员，类型为 {@code OutboxRepository}
     * @param auditLog 业务处理参数或成员，类型为 {@code AuditLogRepository}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param idempotency 业务或技术标识，类型为 {@code IdempotencyPort}
     */
    public InboundTrackingApplicationService(InboundTrackingRepository repository, OutboxRepository outbox, AuditLogRepository auditLog, IdentifierGenerator ids, IdempotencyPort idempotency) {
        this.repository = repository;
        this.outbox = outbox;
        this.auditLog = auditLog;
        this.ids = ids;
        this.idempotency = idempotency;
    }

    /**
     * 处理当前类型职责中的操作 {@code recordAsn}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code InboundCommands.RecordAsn}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult recordAsn(InboundCommands.RecordAsn command, CommandContext context) {
        context.requirePermission("purchase:inbound:record-asn");
        context.requirePurchaseOrgScope(command.purchaseOrgId());
        return idempotency.execute("purchase:inbound:record-asn", context, () -> {
            repository.findByAsnNo(command.asnNo()).ifPresent(existing -> {
                throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "ASN已存在到货跟踪");
            });
            var aggregate = InboundTrackingAggregate.recordAsn(command.orderNo(), command.asnNo(), command.supplierId(), command.purchaseOrgId(), command.warehouseCode(), command.skuCode(), command.notifiedQty(), ids);
            return persist(aggregate, context, "RECORD_ASN", null);
        });
    }

    /**
     * 处理当前类型职责中的操作 {@code syncWms}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param inboundNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code InboundCommands.SyncWms}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult syncWms(String inboundNo, InboundCommands.SyncWms command, CommandContext context) {
        context.requirePermission("purchase:inbound:sync-wms");
        return idempotency.execute("purchase:inbound:sync-wms", context, () -> {
            var aggregate = repository.findByNo(inboundNo).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "到货跟踪不存在"));
            context.requirePurchaseOrgScope(aggregate.purchaseOrgId());
            if (aggregate.version() != command.version()) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "到货跟踪已被其他人修改");
            }
            var before = snapshot(aggregate);
            aggregate.syncWms(command.receivedQty(), command.qualifiedQty(), command.unqualifiedQty(), command.putawayQty(), command.reason(), ids);
            return persist(aggregate, context, "SYNC_WMS_INBOUND", before);
        });
    }

    /**
     * 处理当前类型职责中的操作 {@code syncWmsFromEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param inboundNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code InboundCommands.SyncWms}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult syncWmsFromEvent(String inboundNo, InboundCommands.SyncWms command, CommandContext context) {
        context.requirePermission("purchase:inbound:sync-wms");
        return idempotency.execute("purchase:inbound:sync-wms:event", context, () -> {
            var aggregate = repository.findByNo(inboundNo).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "到货跟踪不存在"));
            context.requirePurchaseOrgScope(aggregate.purchaseOrgId());
            var before = snapshot(aggregate);
            aggregate.syncWms(command.receivedQty(), command.qualifiedQty(), command.unqualifiedQty(), command.putawayQty(), command.reason(), ids);
            return persist(aggregate, context, "CONSUME_WMS_INBOUND", before);
        });
    }

    /**
     * 处理当前类型职责中的操作 {@code persist}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code InboundTrackingAggregate}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @param operation 业务处理参数或成员，类型为 {@code String}
     * @param before 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private CommandResult persist(InboundTrackingAggregate aggregate, CommandContext context, String operation, String before) {
        repository.save(aggregate, context.operatorId());
        var events = aggregate.pullEvents();
        outbox.saveAll(events);
        auditLog.save(context, operation, "INBOUND_TRACKING", aggregate.id(), aggregate.inboundNo(), before, snapshot(aggregate));
        var eventCode = events.isEmpty() ? null : events.get(events.size() - 1).eventCode();
        return new CommandResult(aggregate.id(), aggregate.inboundNo(), aggregate.status().code(), aggregate.status().label(), aggregate.version(), eventCode, false);
    }

    /**
     * 处理当前类型职责中的操作 {@code snapshot}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code InboundTrackingAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String snapshot(InboundTrackingAggregate aggregate) {
        return "{\"inboundNo\":\"%s\",\"status\":%d,\"version\":%d}".formatted(aggregate.inboundNo(), aggregate.status().code(), aggregate.version());
    }
}
