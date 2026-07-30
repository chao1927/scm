package com.chaobo.scm.purchase.application.supplierreturn;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.application.integration.IntegrationCommandEnqueuer;
import com.chaobo.scm.purchase.application.shared.*;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import com.chaobo.scm.purchase.domain.supplierreturn.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

/**
 * SupplierReturnApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class SupplierReturnApplicationService {

    /**
     * repository（类型：{@code SupplierReturnRepository}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierReturnRepository repository;

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
     * integrations（类型：{@code IntegrationCommandEnqueuer}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final IntegrationCommandEnqueuer integrations;

    /**
     * 创建 SupplierReturnApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param repository 持久化访问依赖，类型为 {@code SupplierReturnRepository}
     * @param outbox 业务处理参数或成员，类型为 {@code OutboxRepository}
     * @param auditLog 业务处理参数或成员，类型为 {@code AuditLogRepository}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param idempotency 业务或技术标识，类型为 {@code IdempotencyPort}
     * @param integrations 业务处理参数或成员，类型为 {@code IntegrationCommandEnqueuer}
     */
    public SupplierReturnApplicationService(SupplierReturnRepository repository, OutboxRepository outbox, AuditLogRepository auditLog, IdentifierGenerator ids, IdempotencyPort idempotency, IntegrationCommandEnqueuer integrations) {
        this.repository = repository;
        this.outbox = outbox;
        this.auditLog = auditLog;
        this.ids = ids;
        this.idempotency = idempotency;
        this.integrations = integrations;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code SupplierReturnCommands.Create}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult create(SupplierReturnCommands.Create command, CommandContext context) {
        context.requirePermission("purchase:supplier-return:create");
        context.requirePurchaseOrgScope(command.purchaseOrgId());
        return idempotency.execute("purchase:supplier-return:create", context, () -> persist(SupplierReturnAggregate.create(command.sourceOrderNo(), command.supplierId(), command.purchaseOrgId(), command.warehouseCode(), lines(command.lines()), ids), context, "CREATE_SUPPLIER_RETURN", null));
    }

    /**
     * 执行命令 {@code submit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param returnNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code SupplierReturnCommands.Version}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult submit(String returnNo, SupplierReturnCommands.Version command, CommandContext context) {
        context.requirePermission("purchase:supplier-return:submit");
        return change(returnNo, command.version(), context, "SUBMIT_SUPPLIER_RETURN", aggregate -> aggregate.submit(ids));
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param returnNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code SupplierReturnCommands.Approve}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult approve(String returnNo, SupplierReturnCommands.Approve command, CommandContext context) {
        context.requirePermission("purchase:supplier-return:approve");
        return change(returnNo, command.version(), context, "APPROVE_SUPPLIER_RETURN", aggregate -> aggregate.approve(command.approved(), command.reason(), ids));
    }

    /**
     * 处理当前类型职责中的操作 {@code notifyExecution}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param returnNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code SupplierReturnCommands.Notify}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult notifyExecution(String returnNo, SupplierReturnCommands.Notify command, CommandContext context) {
        context.requirePermission("purchase:supplier-return:notify");
        return idempotency.execute("purchase:supplier-return:NOTIFY_SUPPLIER_RETURN", context, () -> {
            var aggregate = repository.findByNo(returnNo).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "退供申请不存在"));
            context.requirePurchaseOrgScope(aggregate.purchaseOrgId());
            if (aggregate.version() != command.version()) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "退供申请已被其他人修改");
            }
            var before = snapshot(aggregate);
            aggregate.notifyExecution(command.notifyMode(), ids);
            var result = persist(aggregate, context, "NOTIFY_SUPPLIER_RETURN", before);
            enqueueExecutionCommands(aggregate);
            return result;
        });
    }

    /**
     * 处理当前类型职责中的操作 {@code change}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param returnNo 可追踪业务编码，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @param operation 业务处理参数或成员，类型为 {@code String}
     * @param action 业务处理参数或成员，类型为 {@code java.util.function.Consumer<SupplierReturnAggregate>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private CommandResult change(String returnNo, int version, CommandContext context, String operation, java.util.function.Consumer<SupplierReturnAggregate> action) {
        return idempotency.execute("purchase:supplier-return:" + operation, context, () -> {
            var aggregate = repository.findByNo(returnNo).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "退供申请不存在"));
            context.requirePurchaseOrgScope(aggregate.purchaseOrgId());
            if (aggregate.version() != version) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "退供申请已被其他人修改");
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
     * @param aggregate 业务处理参数或成员，类型为 {@code SupplierReturnAggregate}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @param operation 业务处理参数或成员，类型为 {@code String}
     * @param before 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private CommandResult persist(SupplierReturnAggregate aggregate, CommandContext context, String operation, String before) {
        repository.save(aggregate, context.operatorId());
        var events = aggregate.pullEvents();
        outbox.saveAll(events);
        auditLog.save(context, operation, "SUPPLIER_RETURN", aggregate.id(), aggregate.returnNo(), before, snapshot(aggregate));
        var eventCode = events.isEmpty() ? null : events.get(events.size() - 1).eventCode();
        return new CommandResult(aggregate.id(), aggregate.returnNo(), aggregate.status().code(), aggregate.status().label(), aggregate.version(), eventCode, false);
    }

    /**
     * 处理当前类型职责中的操作 {@code lines}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param commands 用例输入命令，类型为 {@code List<SupplierReturnCommands.Line>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<SupplierReturnLine>}
     */
    private List<SupplierReturnLine> lines(List<SupplierReturnCommands.Line> commands) {
        if (commands == null || commands.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "退供行不能为空");
        }
        return commands.stream().map(line -> new SupplierReturnLine(line.lineId() == null ? ids.nextId() : line.lineId(), line.skuCode(), line.returnQty(), line.returnableQty(), line.reason())).toList();
    }

    /**
     * 处理当前类型职责中的操作 {@code snapshot}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code SupplierReturnAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String snapshot(SupplierReturnAggregate aggregate) {
        return "{\"returnNo\":\"%s\",\"status\":%d,\"version\":%d}".formatted(aggregate.returnNo(), aggregate.status().code(), aggregate.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code enqueueExecutionCommands}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code SupplierReturnAggregate}
     */
    private void enqueueExecutionCommands(SupplierReturnAggregate aggregate) {
        var payload = Map.of("returnId", aggregate.id(), "returnNo", aggregate.returnNo(), "sourceOrderNo", aggregate.sourceOrderNo(), "supplierId", aggregate.supplierId(), "purchaseOrgId", aggregate.purchaseOrgId(), "warehouseCode", aggregate.warehouseCode() == null ? "" : aggregate.warehouseCode(), "version", aggregate.version(), "lines", aggregate.lines().stream().map(line -> Map.of("lineId", line.lineId(), "skuCode", line.skuCode(), "returnQty", line.returnQty(), "returnableQty", line.returnableQty(), "reason", line.reason() == null ? "" : line.reason())).toList());
        integrations.enqueue("INVENTORY_LOCK_SUPPLIER_RETURN", "INVENTORY", "SUPPLIER_RETURN", Long.toString(aggregate.id()), aggregate.returnNo(), payload);
        integrations.enqueue("WMS_CREATE_SUPPLIER_RETURN_OUTBOUND", "WMS", "SUPPLIER_RETURN", Long.toString(aggregate.id()), aggregate.returnNo(), payload);
        integrations.enqueue("TMS_CREATE_SUPPLIER_RETURN_TRANSPORT", "TMS", "SUPPLIER_RETURN", Long.toString(aggregate.id()), aggregate.returnNo(), payload);
        integrations.enqueue("BMS_CREATE_SUPPLIER_RETURN_OFFSET", "BMS", "SUPPLIER_RETURN", Long.toString(aggregate.id()), aggregate.returnNo(), payload);
    }
}
