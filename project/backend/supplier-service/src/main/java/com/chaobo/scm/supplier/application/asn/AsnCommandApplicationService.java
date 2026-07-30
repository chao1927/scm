package com.chaobo.scm.supplier.application.asn;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.supplier.domain.asn.AsnAggregate;
import com.chaobo.scm.supplier.domain.asn.AsnRepository;
import com.chaobo.scm.supplier.domain.shared.DomainEvent;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import com.chaobo.scm.supplier.application.shared.AuditLogRepository;
import com.chaobo.scm.supplier.application.shared.CommandContext;
import com.chaobo.scm.supplier.application.shared.CommandResult;
import com.chaobo.scm.supplier.application.shared.OutboxRepository;
import com.chaobo.scm.supplier.application.shared.TransactionalCommandExecutor;
import com.chaobo.scm.supplier.application.integration.IntegrationCommandEnqueuer;
import com.chaobo.scm.common.integration.WmsCollaborationApi;
import com.chaobo.scm.common.integration.TmsCollaborationApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * AsnCommandApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class AsnCommandApplicationService {

    /**
     * repository（类型：{@code AsnRepository}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final AsnRepository repository;

    /**
     * outboxRepository（类型：{@code OutboxRepository}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final OutboxRepository outboxRepository;

    /**
     * auditLogRepository（类型：{@code AuditLogRepository}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final AuditLogRepository auditLogRepository;

    /**
     * commandExecutor（类型：{@code TransactionalCommandExecutor}）。
     *
     * <p>保存当前对象所需的用例输入命令；其具体生命周期由所属对象统一管理。
     */
    private final TransactionalCommandExecutor commandExecutor;

    /**
     * identifierGenerator（类型：{@code IdentifierGenerator}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final IdentifierGenerator identifierGenerator;

    /**
     * integrations（类型：{@code IntegrationCommandEnqueuer}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final IntegrationCommandEnqueuer integrations;

    /**
     * 创建 AsnCommandApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param repository 持久化访问依赖，类型为 {@code AsnRepository}
     * @param outboxRepository 持久化访问依赖，类型为 {@code OutboxRepository}
     * @param auditLogRepository 持久化访问依赖，类型为 {@code AuditLogRepository}
     * @param commandExecutor 用例输入命令，类型为 {@code TransactionalCommandExecutor}
     * @param identifierGenerator 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param integrations 业务处理参数或成员，类型为 {@code IntegrationCommandEnqueuer}
     */
    public AsnCommandApplicationService(AsnRepository repository, OutboxRepository outboxRepository, AuditLogRepository auditLogRepository, TransactionalCommandExecutor commandExecutor, IdentifierGenerator identifierGenerator, IntegrationCommandEnqueuer integrations) {
        this.repository = repository;
        this.outboxRepository = outboxRepository;
        this.auditLogRepository = auditLogRepository;
        this.commandExecutor = commandExecutor;
        this.identifierGenerator = identifierGenerator;
        this.integrations = integrations;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code AsnCommands.Create}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult create(AsnCommands.Create command, CommandContext context) {
        context.requirePermission("supplier:asn:create");
        context.requireSupplierScope(command.supplierId());
        return commandExecutor.execute("supplier:asn", context, command, () -> {
            AsnAggregate aggregate = AsnAggregate.create(command.purchaseOrderId(), command.supplierId(), command.warehouseId(), command.estimatedArrivalAt(), command.lines(), context.operatorId(), identifierGenerator);
            return persist(aggregate, context, "CREATE_ASN", null);
        });
    }

    /**
     * 执行命令 {@code submit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code AsnCommands.Submit}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult submit(AsnCommands.Submit command, CommandContext context) {
        context.requirePermission("supplier:asn:submit");
        return change(command.asnId(), command.version(), command, context, "SUBMIT_ASN", aggregate -> aggregate.submit(context.operatorId(), identifierGenerator));
    }

    /**
     * 执行命令 {@code cancel}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code AsnCommands.Cancel}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult cancel(AsnCommands.Cancel command, CommandContext context) {
        context.requirePermission("supplier:asn:cancel");
        return change(command.asnId(), command.version(), command, context, "CANCEL_ASN", aggregate -> aggregate.cancel(command.reason(), context.operatorId(), identifierGenerator));
    }

    /**
     * 执行命令 {@code confirmShipment}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code AsnCommands.ConfirmShipment}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult confirmShipment(AsnCommands.ConfirmShipment command, CommandContext context) {
        context.requirePermission("supplier:asn:ship");
        return change(command.asnId(), command.version(), command, context, "SHIP_ASN", aggregate -> aggregate.confirmShipment(command.shipmentInfo(), context.operatorId(), identifierGenerator));
    }

    /**
     * 处理当前类型职责中的操作 {@code change}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param asnId 业务或技术标识，类型为 {@code long}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code int}
     * @param command 用例输入命令，类型为 {@code Object}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @param operation 业务处理参数或成员，类型为 {@code String}
     * @param action 业务处理参数或成员，类型为 {@code java.util.function.Consumer<AsnAggregate>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private CommandResult change(long asnId, int expectedVersion, Object command, CommandContext context, String operation, java.util.function.Consumer<AsnAggregate> action) {
        return commandExecutor.execute("supplier:asn", context, command, () -> {
            AsnAggregate aggregate = repository.findById(asnId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "ASN 不存在"));
            context.requireSupplierScope(aggregate.supplierId());
            if (aggregate.version() != expectedVersion) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "ASN 已被其他操作更新，请刷新后重试");
            }
            String before = snapshot(aggregate);
            action.accept(aggregate);
            return persist(aggregate, context, operation, before);
        });
    }

    /**
     * 处理当前类型职责中的操作 {@code persist}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code AsnAggregate}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @param operation 业务处理参数或成员，类型为 {@code String}
     * @param beforeSnapshot 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private CommandResult persist(AsnAggregate aggregate, CommandContext context, String operation, String beforeSnapshot) {
        repository.save(aggregate, context.operatorId());
        List<DomainEvent> events = aggregate.pullEvents();
        outboxRepository.saveAll(events);
        auditLogRepository.save(context, operation, "ASN", aggregate.asnId(), aggregate.asnNo(), beforeSnapshot, snapshot(aggregate));
        if (SUBMIT_ASN.equals(operation)) {
            var lines = aggregate.lines().stream().map(line -> new WmsCollaborationApi.Line(line.lineId(), line.skuCode(), line.batchNo(), line.plannedQuantity())).toList();
            integrations.enqueue("WMS_CREATE_APPOINTMENT", "ASN", aggregate.asnId(), aggregate.version(), "WMS", new WmsCollaborationApi.InboundAppointmentCommand("ASN-APPOINT-" + aggregate.asnId() + "-" + aggregate.version(), aggregate.asnId(), aggregate.asnNo(), aggregate.supplierId(), aggregate.warehouseId(), aggregate.estimatedArrivalAt(), lines));
        } else if (SHIP_ASN.equals(operation)) {
            var shipment = aggregate.shipmentInfo();
            integrations.enqueue("TMS_CREATE_INBOUND_TRANSPORT", "ASN", aggregate.asnId(), aggregate.version(), "TMS", new TmsCollaborationApi.InboundTransportCommand("ASN-TRANSPORT-" + aggregate.asnId() + "-" + aggregate.version(), aggregate.asnId(), aggregate.asnNo(), aggregate.supplierId(), aggregate.warehouseId(), shipment.shippedAt(), shipment.carrierName(), shipment.trackingNo()));
        } else if (CANCEL_ASN.equals(operation)) {
            integrations.enqueue("WMS_CANCEL_APPOINTMENT", "ASN", aggregate.asnId(), aggregate.version(), "WMS", new WmsCollaborationApi.CancelAppointmentCommand("ASN-APPOINT-CANCEL-" + aggregate.asnId() + "-" + aggregate.version(), aggregate.asnId(), aggregate.cancelReason()));
            if (aggregate.shipmentInfo() != null) {
                integrations.enqueue("TMS_CANCEL_TRANSPORT", "ASN", aggregate.asnId(), aggregate.version(), "TMS", new TmsCollaborationApi.CancelTransportCommand("ASN-TRANSPORT-CANCEL-" + aggregate.asnId() + "-" + aggregate.version(), "ASN", aggregate.asnId(), aggregate.cancelReason()));
            }
        }
        String eventCode = events.isEmpty() ? null : events.get(events.size() - 1).eventCode();
        return new CommandResult(aggregate.asnId(), aggregate.asnNo(), aggregate.status().code(), aggregate.status().label(), aggregate.version(), eventCode, false);
    }

    /**
     * 处理当前类型职责中的操作 {@code snapshot}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code AsnAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String snapshot(AsnAggregate aggregate) {
        return "{\"asnNo\":\"%s\",\"status\":\"%s\",\"version\":%d}".formatted(aggregate.asnNo(), aggregate.status().name(), aggregate.version());
    }

    /**
     * 业务常量 {@code CANCEL_ASN}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String CANCEL_ASN = "CANCEL_ASN";

    /**
     * 业务常量 {@code SHIP_ASN}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String SHIP_ASN = "SHIP_ASN";

    /**
     * 业务常量 {@code SUBMIT_ASN}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String SUBMIT_ASN = "SUBMIT_ASN";
}
