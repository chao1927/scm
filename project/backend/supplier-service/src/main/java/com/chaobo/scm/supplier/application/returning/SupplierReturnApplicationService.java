package com.chaobo.scm.supplier.application.returning;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.*;
import com.chaobo.scm.common.integration.*;
import com.chaobo.scm.supplier.application.integration.IntegrationCommandEnqueuer;
import com.chaobo.scm.supplier.application.shared.*;
import com.chaobo.scm.supplier.domain.returning.*;
import com.chaobo.scm.supplier.domain.shared.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;

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
     * repo（类型：{@code SupplierReturnRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierReturnRepository repo;

    /**
     * read（类型：{@code SupplierReturnReadModelPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierReturnReadModelPort read;

    /**
     * outbox（类型：{@code OutboxRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final OutboxRepository outbox;

    /**
     * audit（类型：{@code AuditLogRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AuditLogRepository audit;

    /**
     * ids（类型：{@code IdentifierGenerator}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final IdentifierGenerator ids;

    /**
     * executor（类型：{@code TransactionalCommandExecutor}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final TransactionalCommandExecutor executor;

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
     * @param repo 业务处理参数或成员，类型为 {@code SupplierReturnRepository}
     * @param read 业务处理参数或成员，类型为 {@code SupplierReturnReadModelPort}
     * @param outbox 业务处理参数或成员，类型为 {@code OutboxRepository}
     * @param audit 业务处理参数或成员，类型为 {@code AuditLogRepository}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param executor 业务处理参数或成员，类型为 {@code TransactionalCommandExecutor}
     * @param integrations 业务处理参数或成员，类型为 {@code IntegrationCommandEnqueuer}
     */
    public SupplierReturnApplicationService(SupplierReturnRepository repo, SupplierReturnReadModelPort read, OutboxRepository outbox, AuditLogRepository audit, IdentifierGenerator ids, TransactionalCommandExecutor executor, IntegrationCommandEnqueuer integrations) {
        this.repo = repo;
        this.read = read;
        this.outbox = outbox;
        this.audit = audit;
        this.ids = ids;
        this.executor = executor;
        this.integrations = integrations;
    }

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param scope 业务处理参数或成员，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param page 业务处理参数或成员，类型为 {@code int}
     * @param size 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PageResult<SupplierReturnView>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PageResult<SupplierReturnView> page(Long supplierId, Long scope, Integer status, int page, int size) {
        if (page < 1 || size < 1 || size > PAGE_VALUE_100) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "分页参数不合法");
        }
        return read.page(scope == null ? supplierId : scope, status, page, size);
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param scope 业务处理参数或成员，类型为 {@code Long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierReturnView}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public SupplierReturnView detail(long id, Long scope) {
        var value = read.detail(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "退供单不存在"));
        if (scope != null && scope != value.supplierId()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "退供单不存在");
        }
        return value;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param warehouseId 业务或技术标识，类型为 {@code long}
     * @param qualityIssueId 业务或技术标识，类型为 {@code Long}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param lines 业务处理参数或成员，类型为 {@code List<SupplierReturnAggregate.NewLine>}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult create(long supplierId, long warehouseId, Long qualityIssueId, String reason, List<SupplierReturnAggregate.NewLine> lines, CommandContext c) {
        c.requirePermission("supplier:return:create");
        c.requireSupplierScope(supplierId);
        return executor.execute("supplier:return", c, new Create(supplierId, warehouseId, qualityIssueId, reason, lines), () -> persist(SupplierReturnAggregate.create(supplierId, warehouseId, qualityIssueId, reason, lines, c.operatorId(), ids), c, "CREATE_SUPPLIER_RETURN", null));
    }

    /**
     * 执行命令 {@code submit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult submit(long id, int version, CommandContext c) {
        c.requirePermission("supplier:return:submit");
        return change(id, version, c, "SUBMIT_SUPPLIER_RETURN", a -> a.submit(c.operatorId(), ids));
    }

    /**
     * 处理当前类型职责中的操作 {@code review}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param pass 业务处理参数或成员，类型为 {@code boolean}
     * @param comment 业务处理参数或成员，类型为 {@code String}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult review(long id, int version, boolean pass, String comment, CommandContext c) {
        c.requirePermission("supplier:return:review");
        return change(id, version, c, "REVIEW_SUPPLIER_RETURN", a -> a.review(pass, comment, c.operatorId(), ids));
    }

    /**
     * 处理当前类型职责中的操作 {@code requestInventoryLock}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult requestInventoryLock(long id, int version, CommandContext c) {
        c.requirePermission("supplier:return:inventory_lock");
        return change(id, version, c, "REQUEST_RETURN_INVENTORY_LOCK", a -> a.requestInventoryLock(c.operatorId(), ids));
    }

    /**
     * 处理当前类型职责中的操作 {@code supplierConfirm}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param diff 业务处理参数或成员，类型为 {@code boolean}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult supplierConfirm(long id, int version, boolean diff, String reason, CommandContext c) {
        c.requirePermission("supplier:return:confirm");
        return change(id, version, c, "CONFIRM_SUPPLIER_RETURN", a -> a.supplierConfirm(diff, reason, c.operatorId(), ids));
    }

    /**
     * 处理当前类型职责中的操作 {@code resolveDifference}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult resolveDifference(long id, int version, CommandContext c) {
        c.requirePermission("supplier:return:resolve_difference");
        return change(id, version, c, "RESOLVE_RETURN_DIFFERENCE", a -> a.resolveDifference(c.operatorId(), ids));
    }

    /**
     * 执行命令 {@code close}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult close(long id, int version, CommandContext c) {
        c.requirePermission("supplier:return:close");
        return change(id, version, c, "CLOSE_SUPPLIER_RETURN", a -> a.close(c.operatorId(), ids));
    }

    /**
     * 处理当前类型职责中的操作 {@code recordInventoryLock}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param success 业务处理参数或成员，类型为 {@code boolean}
     * @param lockNo 可追踪业务编码，类型为 {@code String}
     * @param quantities 业务处理参数或成员，类型为 {@code Map<Long,BigDecimal>}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult recordInventoryLock(long id, boolean success, String lockNo, Map<Long, BigDecimal> quantities, String reason, CommandContext c) {
        return externalChange(id, c, "RECORD_RETURN_INVENTORY_LOCK", a -> a.recordInventoryLock(success, lockNo, quantities, reason, c.operatorId(), ids));
    }

    /**
     * 处理当前类型职责中的操作 {@code recordOutbound}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param quantities 业务处理参数或成员，类型为 {@code Map<Long,BigDecimal>}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult recordOutbound(long id, String no, Map<Long, BigDecimal> quantities, CommandContext c) {
        return externalChange(id, c, "RECORD_RETURN_OUTBOUND", a -> a.recordOutbound(no, quantities, c.operatorId(), ids));
    }

    /**
     * 处理当前类型职责中的操作 {@code recordWaybill}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param shipment 业务处理参数或成员，类型为 {@code String}
     * @param waybill 业务处理参数或成员，类型为 {@code String}
     * @param carrier 业务处理参数或成员，类型为 {@code String}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult recordWaybill(long id, String shipment, String waybill, String carrier, CommandContext c) {
        return externalChange(id, c, "RECORD_RETURN_WAYBILL", a -> a.recordWaybill(shipment, waybill, carrier, c.operatorId(), ids));
    }

    /**
     * 处理当前类型职责中的操作 {@code recordSigned}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param quantities 业务处理参数或成员，类型为 {@code Map<Long,BigDecimal>}
     * @param differenceReason 业务处理参数或成员，类型为 {@code String}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult recordSigned(long id, Map<Long, BigDecimal> quantities, String differenceReason, CommandContext c) {
        return externalChange(id, c, "RECORD_RETURN_SIGNED", a -> a.recordSigned(quantities, differenceReason, c.operatorId(), ids));
    }

    /**
     * 处理当前类型职责中的操作 {@code recordTransportException}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult recordTransportException(long id, String reason, CommandContext c) {
        return externalChange(id, c, "RECORD_RETURN_TRANSPORT_EXCEPTION", a -> a.recordTransportException(reason, c.operatorId(), ids));
    }

    /**
     * 处理当前类型职责中的操作 {@code recordSettlement}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param ref 业务处理参数或成员，类型为 {@code String}
     * @param offset 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param claim 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult recordSettlement(long id, String ref, BigDecimal offset, BigDecimal claim, CommandContext c) {
        return externalChange(id, c, "RECORD_RETURN_SETTLEMENT", a -> a.recordSettlement(ref, offset, claim, c.operatorId(), ids));
    }

    /**
     * 处理当前类型职责中的操作 {@code change}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @param operation 业务处理参数或成员，类型为 {@code String}
     * @param action 业务处理参数或成员，类型为 {@code Consumer<SupplierReturnAggregate>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private CommandResult change(long id, int version, CommandContext c, String operation, Consumer<SupplierReturnAggregate> action) {
        return executor.execute("supplier:return", c, new Change(id, version, operation), () -> {
            var aggregate = get(id);
            c.requireSupplierScope(aggregate.supplierId());
            if (aggregate.version() != version) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "退供单已被更新");
            }
            String before = snapshot(aggregate);
            action.accept(aggregate);
            return persist(aggregate, c, operation, before);
        });
    }

    /**
     * 处理当前类型职责中的操作 {@code externalChange}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @param operation 业务处理参数或成员，类型为 {@code String}
     * @param action 业务处理参数或成员，类型为 {@code Consumer<SupplierReturnAggregate>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private CommandResult externalChange(long id, CommandContext c, String operation, Consumer<SupplierReturnAggregate> action) {
        return executor.execute("supplier:return:event", c, new External(id, operation), () -> {
            var aggregate = get(id);
            String before = snapshot(aggregate);
            action.accept(aggregate);
            return persist(aggregate, c, operation, before);
        });
    }

    /**
     * 查询并返回 {@code get}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code SupplierReturnAggregate}
     */
    private SupplierReturnAggregate get(long id) {
        return repo.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "退供单不存在"));
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
        repo.save(aggregate, context.operatorId());
        var events = aggregate.pullEvents();
        outbox.saveAll(events);
        enqueueCollaboration(aggregate, operation);
        audit.save(context, operation, "SUPPLIER_RETURN", aggregate.id(), aggregate.no(), before, snapshot(aggregate));
        return new CommandResult(aggregate.id(), aggregate.no(), aggregate.status().code(), aggregate.status().label(), aggregate.version(), events.isEmpty() ? null : events.get(events.size() - 1).eventCode(), false);
    }

    /**
     * 处理当前类型职责中的操作 {@code enqueueCollaboration}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code SupplierReturnAggregate}
     * @param operation 业务处理参数或成员，类型为 {@code String}
     */
    private void enqueueCollaboration(SupplierReturnAggregate aggregate, String operation) {
        String suffix = aggregate.id() + "-" + aggregate.version();
        if (REQUEST_RETURN_INVENTORY_LOCK.equals(operation)) {
            var lines = aggregate.lines().stream().map(line -> new InventoryCollaborationApi.Line(line.id(), line.skuCode(), line.batchNo(), line.inventoryStatus(), line.requestedQty())).toList();
            integrations.enqueue("INVENTORY_LOCK_RETURN", "SUPPLIER_RETURN", aggregate.id(), aggregate.version(), "INVENTORY", new InventoryCollaborationApi.ReturnLockCommand("RETURN-LOCK-" + suffix, aggregate.id(), aggregate.no(), aggregate.supplierId(), aggregate.warehouseId(), lines));
        } else if (CONFIRM_SUPPLIER_RETURN.equals(operation) && aggregate.status() == SupplierReturnStatus.PENDING_OUTBOUND) {
            var lines = aggregate.lines().stream().map(line -> new WmsCollaborationApi.Line(line.id(), line.skuCode(), line.batchNo(), line.lockedQty())).toList();
            integrations.enqueue("WMS_CREATE_RETURN_OUTBOUND", "SUPPLIER_RETURN", aggregate.id(), aggregate.version(), "WMS", new WmsCollaborationApi.ReturnOutboundCommand("RETURN-OUTBOUND-" + suffix, aggregate.id(), aggregate.no(), aggregate.supplierId(), aggregate.warehouseId(), aggregate.inventoryLockNo(), lines));
        } else if (CONFIRM_SUPPLIER_RETURN.equals(operation) && aggregate.status() == SupplierReturnStatus.SUPPLIER_DIFFERENCE && aggregate.inventoryLockNo() != null) {
            integrations.enqueue("INVENTORY_RELEASE_RETURN", "SUPPLIER_RETURN", aggregate.id(), aggregate.version(), "INVENTORY", new InventoryCollaborationApi.ReturnReleaseCommand("RETURN-RELEASE-" + suffix, aggregate.id(), aggregate.inventoryLockNo(), "供应商拒绝或提出退供差异"));
        } else if (RECORD_RETURN_OUTBOUND.equals(operation)) {
            integrations.enqueue("TMS_CREATE_RETURN_TRANSPORT", "SUPPLIER_RETURN", aggregate.id(), aggregate.version(), "TMS", new TmsCollaborationApi.ReturnTransportCommand("RETURN-TRANSPORT-" + suffix, aggregate.id(), aggregate.no(), aggregate.supplierId(), aggregate.warehouseId(), aggregate.outboundNo()));
        } else if (RECORD_RETURN_SIGNED.equals(operation) && aggregate.status() == SupplierReturnStatus.SIGNED) {
            integrations.enqueue("BMS_CREATE_RETURN_SETTLEMENT", "SUPPLIER_RETURN", aggregate.id(), aggregate.version(), "BMS", new BmsCollaborationApi.ReturnSettlementCommand("RETURN-SETTLEMENT-" + suffix, aggregate.id(), aggregate.no(), aggregate.supplierId(), BigDecimal.ZERO, BigDecimal.ZERO, "按退供单和合同价格计算冲减"));
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code snapshot}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code SupplierReturnAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String snapshot(SupplierReturnAggregate aggregate) {
        return "{\"returnNo\":\"%s\",\"status\":%d,\"version\":%d}".formatted(aggregate.no(), aggregate.status().code(), aggregate.version());
    }

    /**
     * Create。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private record Create(long supplierId, long warehouseId, Long qualityIssueId, String reason, List<SupplierReturnAggregate.NewLine> lines) {
    }

    /**
     * Change。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private record Change(long id, int version, String operation) {
    }

    /**
     * External。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private record External(long id, String operation) {
    }

    /**
     * 业务常量 {@code RECORD_RETURN_SIGNED}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String RECORD_RETURN_SIGNED = "RECORD_RETURN_SIGNED";

    /**
     * 业务常量 {@code RECORD_RETURN_OUTBOUND}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String RECORD_RETURN_OUTBOUND = "RECORD_RETURN_OUTBOUND";

    /**
     * 业务常量 {@code CONFIRM_SUPPLIER_RETURN}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String CONFIRM_SUPPLIER_RETURN = "CONFIRM_SUPPLIER_RETURN";

    /**
     * 业务常量 {@code REQUEST_RETURN_INVENTORY_LOCK}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String REQUEST_RETURN_INVENTORY_LOCK = "REQUEST_RETURN_INVENTORY_LOCK";

    /**
     * 业务常量 {@code PAGE_VALUE_100}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int PAGE_VALUE_100 = 100;
}
