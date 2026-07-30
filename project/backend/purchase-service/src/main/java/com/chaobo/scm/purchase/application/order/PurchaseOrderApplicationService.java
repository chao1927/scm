package com.chaobo.scm.purchase.application.order;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.application.integration.IntegrationCommandEnqueuer;
import com.chaobo.scm.purchase.application.shared.*;
import com.chaobo.scm.purchase.domain.order.*;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * PurchaseOrderApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class PurchaseOrderApplicationService {

    /**
     * repository（类型：{@code PurchaseOrderRepository}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final PurchaseOrderRepository repository;

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
     * 创建 PurchaseOrderApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param repository 持久化访问依赖，类型为 {@code PurchaseOrderRepository}
     * @param outbox 业务处理参数或成员，类型为 {@code OutboxRepository}
     * @param auditLog 业务处理参数或成员，类型为 {@code AuditLogRepository}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param idempotency 业务或技术标识，类型为 {@code IdempotencyPort}
     * @param integrations 业务处理参数或成员，类型为 {@code IntegrationCommandEnqueuer}
     */
    public PurchaseOrderApplicationService(PurchaseOrderRepository repository, OutboxRepository outbox, AuditLogRepository auditLog, IdentifierGenerator ids, IdempotencyPort idempotency, IntegrationCommandEnqueuer integrations) {
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
     * @param command 用例输入命令，类型为 {@code PurchaseOrderCommands.Create}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult create(PurchaseOrderCommands.Create command, CommandContext context) {
        context.requirePermission("purchase:po:create");
        context.requirePurchaseOrgScope(command.purchaseOrgId());
        return idempotency.execute("purchase:po:create", context, () -> persist(PurchaseOrderAggregate.create(command.purchaseType(), command.supplierId(), command.supplierCode(), command.supplierName(), command.purchaseOrgId(), command.warehouseCode(), command.currency(), lines(command.lines()), ids), context, "CREATE_PO", null));
    }

    /**
     * 执行命令 {@code submit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code PurchaseOrderCommands.Version}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult submit(String orderNo, PurchaseOrderCommands.Version command, CommandContext context) {
        context.requirePermission("purchase:po:submit");
        return change(orderNo, command.version(), context, "SUBMIT_PO", order -> order.submit(ids));
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code PurchaseOrderCommands.Approve}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult approve(String orderNo, PurchaseOrderCommands.Approve command, CommandContext context) {
        context.requirePermission("purchase:po:approve");
        return change(orderNo, command.version(), context, "APPROVE_PO", order -> order.approve(command.approved(), command.reason(), ids));
    }

    /**
     * 执行命令 {@code publish}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code PurchaseOrderCommands.Publish}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult publish(String orderNo, PurchaseOrderCommands.Publish command, CommandContext context) {
        context.requirePermission("purchase:po:publish");
        return idempotency.execute("purchase:po:PUBLISH_PO", context, () -> {
            var order = load(orderNo);
            context.requirePurchaseOrgScope(order.purchaseOrgId());
            if (order.version() != command.version()) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "采购订单已被其他人修改");
            }
            var before = snapshot(order);
            order.publish(command.publishMode(), ids);
            var result = persist(order, context, "PUBLISH_PO", before);
            enqueuePublishCommands(order);
            return result;
        });
    }

    /**
     * 执行命令 {@code cancel}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code PurchaseOrderCommands.Cancel}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult cancel(String orderNo, PurchaseOrderCommands.Cancel command, CommandContext context) {
        context.requirePermission("purchase:po:cancel");
        return change(orderNo, command.version(), context, "CANCEL_PO", order -> order.cancel(command.reason(), ids));
    }

    /**
     * 执行命令 {@code close}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code PurchaseOrderCommands.Close}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult close(String orderNo, PurchaseOrderCommands.Close command, CommandContext context) {
        context.requirePermission("purchase:po:close");
        return change(orderNo, command.version(), context, "CLOSE_PO", order -> order.closeRemaining(command.reason(), ids));
    }

    /**
     * 处理供应商系统投递的确认、拒绝和差异事实。该入口只供本地 Inbox 消费器调用，
     * 消息幂等由 Inbox 保证，聚合版本由本地订单状态机维护。
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("PMD.SwitchStatementRule")
    public CommandResult recordSupplierResponse(String orderNo, long supplierId, SupplierResponseType responseType, String reason, CommandContext context) {
        var order = load(orderNo);
        context.requirePurchaseOrgScope(order.purchaseOrgId());
        if (order.supplierId() != supplierId) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "供应商确认事件与采购订单供应商不一致");
        }
        var before = snapshot(order);
        switch(responseType) {
            case CONFIRMED ->
                order.recordSupplierConfirmation(reason, ids);
            case REJECTED ->
                order.recordSupplierRejection(reason, ids);
            case DIFFERENCE ->
                order.recordSupplierDifference(reason, ids);
            // 未知类型由上层兼容策略忽略，避免影响已支持事件的消费。
            default ->
                {
                }
        }
        return persist(order, context, "RECORD_SUPPLIER_" + responseType.name(), before);
    }

    /**
     * 处理当前类型职责中的操作 {@code acceptSupplierDifference}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param comment 业务处理参数或成员，类型为 {@code String}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult acceptSupplierDifference(String orderNo, String comment, CommandContext context) {
        return change(orderNo, null, context, "ACCEPT_SUPPLIER_DIFFERENCE", order -> order.acceptSupplierDifference(comment, ids));
    }

    /**
     * 处理当前类型职责中的操作 {@code restartSupplierNegotiation}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param requirement 业务处理参数或成员，类型为 {@code String}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult restartSupplierNegotiation(String orderNo, String requirement, CommandContext context) {
        return change(orderNo, null, context, "RESTART_SUPPLIER_NEGOTIATION", order -> order.restartSupplierNegotiation(requirement, ids));
    }

    /**
     * 执行命令 {@code cancelFromSupplierResponse}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult cancelFromSupplierResponse(String orderNo, String reason, CommandContext context) {
        return change(orderNo, null, context, "CANCEL_PO_FROM_SUPPLIER_RESPONSE", order -> order.cancel(reason, ids));
    }

    /**
     * 执行命令 {@code applyChange}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param lineQtyChanges 数量值，类型为 {@code java.util.Map<Long,BigDecimal>}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     */
    @Transactional(rollbackFor = Exception.class)
    public void applyChange(String orderNo, java.util.Map<Long, BigDecimal> lineQtyChanges, CommandContext context) {
        var order = load(orderNo);
        context.requirePurchaseOrgScope(order.purchaseOrgId());
        var before = snapshot(order);
        order.applyLineQtyChanges(lineQtyChanges, ids);
        persist(order, context, "APPLY_PO_CHANGE", before);
    }

    /**
     * 处理当前类型职责中的操作 {@code change}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code Integer}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @param operation 业务处理参数或成员，类型为 {@code String}
     * @param action 业务处理参数或成员，类型为 {@code java.util.function.Consumer<PurchaseOrderAggregate>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private CommandResult change(String orderNo, Integer version, CommandContext context, String operation, java.util.function.Consumer<PurchaseOrderAggregate> action) {
        return idempotency.execute("purchase:po:" + operation, context, () -> {
            var order = load(orderNo);
            context.requirePurchaseOrgScope(order.purchaseOrgId());
            if (version != null && order.version() != version) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "采购订单已被其他人修改");
            }
            var before = snapshot(order);
            action.accept(order);
            return persist(order, context, operation, before);
        });
    }

    /**
     * 查询并返回 {@code load}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code PurchaseOrderAggregate}
     */
    private PurchaseOrderAggregate load(String orderNo) {
        return repository.findByNo(orderNo).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "采购订单不存在"));
    }

    /**
     * 处理当前类型职责中的操作 {@code persist}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param order 业务处理参数或成员，类型为 {@code PurchaseOrderAggregate}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @param operation 业务处理参数或成员，类型为 {@code String}
     * @param before 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private CommandResult persist(PurchaseOrderAggregate order, CommandContext context, String operation, String before) {
        repository.save(order, context.operatorId());
        var events = order.pullEvents();
        outbox.saveAll(events);
        auditLog.save(context, operation, "PURCHASE_ORDER", order.id(), order.orderNo(), before, snapshot(order));
        var eventCode = events.isEmpty() ? null : events.get(events.size() - 1).eventCode();
        return new CommandResult(order.id(), order.orderNo(), order.status().code(), order.status().label(), order.version(), eventCode, false);
    }

    /**
     * 处理当前类型职责中的操作 {@code lines}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param lines 业务处理参数或成员，类型为 {@code List<PurchaseOrderCommands.Line>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<PurchaseOrderLine>}
     */
    private List<PurchaseOrderLine> lines(List<PurchaseOrderCommands.Line> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "采购订单行不能为空");
        }
        return lines.stream().map(line -> new PurchaseOrderLine(line.lineId() == null ? ids.nextId() : line.lineId(), line.skuCode(), line.skuName(), line.orderQty(), line.unitPrice(), line.taxRate(), null, line.requiredDeliveryDate(), BigDecimal.ZERO)).toList();
    }

    /**
     * 处理当前类型职责中的操作 {@code snapshot}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param order 业务处理参数或成员，类型为 {@code PurchaseOrderAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String snapshot(PurchaseOrderAggregate order) {
        return "{\"orderNo\":\"%s\",\"status\":%d,\"version\":%d}".formatted(order.orderNo(), order.status().code(), order.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code enqueuePublishCommands}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param order 业务处理参数或成员，类型为 {@code PurchaseOrderAggregate}
     */
    private void enqueuePublishCommands(PurchaseOrderAggregate order) {
        var payload = Map.of("orderId", order.id(), "orderNo", order.orderNo(), "supplierId", order.supplierId(), "purchaseOrgId", order.purchaseOrgId(), "warehouseCode", order.warehouseCode() == null ? "" : order.warehouseCode(), "currency", order.currency(), "version", order.version(), "lines", order.lines().stream().map(line -> Map.of("lineId", line.lineId(), "skuCode", line.skuCode(), "orderQty", line.orderQty(), "unitPrice", line.unitPrice(), "requiredDeliveryDate", line.requiredDeliveryDate() == null ? "" : line.requiredDeliveryDate().toString())).toList());
        integrations.enqueue("SUPPLIER_CREATE_PO_CONFIRM_TODO", "SUPPLIER", "PURCHASE_ORDER", Long.toString(order.id()), order.orderNo(), payload);
        integrations.enqueue("WMS_CREATE_PURCHASE_INBOUND_PLAN", "WMS", "PURCHASE_ORDER", Long.toString(order.id()), order.orderNo(), payload);
        integrations.enqueue("BMS_CREATE_PURCHASE_PAYABLE_PLAN", "BMS", "PURCHASE_ORDER", Long.toString(order.id()), order.orderNo(), payload);
    }

    /**
     * SupplierResponseType。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。定义封闭的业务状态或类别集合，避免使用含义不明的数字和字符串。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public enum SupplierResponseType {

        // 业务枚举值：confirmed
        CONFIRMED,
        // 业务枚举值：rejected
        REJECTED,
        // 业务枚举值：difference
        DIFFERENCE
    }
}
