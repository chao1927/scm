package com.chaobo.scm.supplier.application.item;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.application.shared.*;
import com.chaobo.scm.supplier.domain.item.*;
import com.chaobo.scm.supplier.domain.shared.*;
import com.chaobo.scm.supplier.application.masterdata.MasterDataSnapshotPort;
import com.chaobo.scm.supplier.application.qualification.SupplierQualificationPolicyPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.function.Consumer;

/**
 * SupplierItemApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class SupplierItemApplicationService {

    /**
     * repo（类型：{@code SupplierItemRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierItemRepository repo;

    /**
     * read（类型：{@code SupplierItemReadModelPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierItemReadModelPort read;

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
     * masterData（类型：{@code MasterDataSnapshotPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final MasterDataSnapshotPort masterData;

    /**
     * qualifications（类型：{@code SupplierQualificationPolicyPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierQualificationPolicyPort qualifications;

    /**
     * history（类型：{@code SupplierItemHistoryPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierItemHistoryPort history;

    /**
     * 创建 SupplierItemApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param repo 业务处理参数或成员，类型为 {@code SupplierItemRepository}
     * @param read 业务处理参数或成员，类型为 {@code SupplierItemReadModelPort}
     * @param outbox 业务处理参数或成员，类型为 {@code OutboxRepository}
     * @param audit 业务处理参数或成员，类型为 {@code AuditLogRepository}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param executor 业务处理参数或成员，类型为 {@code TransactionalCommandExecutor}
     * @param masterData 业务处理参数或成员，类型为 {@code MasterDataSnapshotPort}
     * @param qualifications 业务处理参数或成员，类型为 {@code SupplierQualificationPolicyPort}
     * @param history 业务处理参数或成员，类型为 {@code SupplierItemHistoryPort}
     */
    public SupplierItemApplicationService(SupplierItemRepository repo, SupplierItemReadModelPort read, OutboxRepository outbox, AuditLogRepository audit, IdentifierGenerator ids, TransactionalCommandExecutor executor, MasterDataSnapshotPort masterData, SupplierQualificationPolicyPort qualifications, SupplierItemHistoryPort history) {
        this.repo = repo;
        this.read = read;
        this.outbox = outbox;
        this.audit = audit;
        this.ids = ids;
        this.executor = executor;
        this.masterData = masterData;
        this.qualifications = qualifications;
        this.history = history;
    }

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param scope 业务处理参数或成员，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param keyword 业务处理参数或成员，类型为 {@code String}
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PageResult<SupplierItemView>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PageResult<SupplierItemView> page(Long supplierId, Long scope, Integer status, String keyword, int pageNo, int pageSize) {
        validatePage(pageNo, pageSize);
        return read.page(scope == null ? supplierId : scope, status, keyword, pageNo, pageSize);
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param scope 业务处理参数或成员，类型为 {@code Long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierItemView}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public SupplierItemView detail(long id, Long scope) {
        var v = read.detail(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "供应商商品不存在"));
        if (scope != null && scope != v.supplierId()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "供应商商品不存在");
        }
        return v;
    }

    /**
     * 执行命令 {@code enable}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param sku 业务处理参数或成员，类型为 {@code String}
     * @param supplierSku 业务处理参数或成员，类型为 {@code String}
     * @param condition 业务处理参数或成员，类型为 {@code SupplyCondition}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult enable(long supplierId, String sku, String supplierSku, SupplyCondition condition, CommandContext c) {
        c.requirePermission("supplier:supplierproduct:create");
        c.requireSupplierScope(supplierId);
        var request = new EnableRequest(supplierId, sku, supplierSku, condition);
        return executor.execute("supplier:item", c, request, () -> {
            ensureEligible(supplierId, sku);
            if (repo.exists(supplierId, sku)) {
                throw new BusinessException(ErrorCode.STATE_CONFLICT, "供应商SKU供货关系已存在");
            }
            return persist(SupplierItemAggregate.enable(supplierId, sku, supplierSku, condition, c.operatorId(), ids), c, "ENABLE_SUPPLIER_ITEM", null);
        });
    }

    /**
     * 处理当前类型职责中的操作 {@code change}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param supplierSku 业务处理参数或成员，类型为 {@code String}
     * @param condition 业务处理参数或成员，类型为 {@code SupplyCondition}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult change(long id, int version, String supplierSku, SupplyCondition condition, CommandContext c) {
        c.requirePermission("supplier:supplierproduct:supplier_sku");
        return change(id, version, new ChangeRequest(id, version, supplierSku, condition), c, "CHANGE_SUPPLY_CONDITION", a -> a.changeCondition(supplierSku, condition, c.operatorId(), ids));
    }

    /**
     * 处理当前类型职责中的操作 {@code pause}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult pause(long id, int version, String reason, CommandContext c) {
        c.requirePermission("supplier:supplierproduct:pause");
        return change(id, version, new StateRequest(id, version, reason), c, "PAUSE_SUPPLY", a -> a.pause(reason, c.operatorId(), ids));
    }

    /**
     * 处理当前类型职责中的操作 {@code resume}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult resume(long id, int version, CommandContext c) {
        c.requirePermission("supplier:supplierproduct:resume");
        return change(id, version, new StateRequest(id, version, "resume"), c, "RESUME_SUPPLY", a -> {
            ensureEligible(a.supplierId(), a.skuCode());
            a.resume(c.operatorId(), ids);
        });
    }

    /**
     * 处理当前类型职责中的操作 {@code discontinue}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult discontinue(long id, int version, String reason, CommandContext c) {
        c.requirePermission("supplier:supplierproduct:discontinue");
        return change(id, version, new StateRequest(id, version, reason), c, "DISCONTINUE_SUPPLY", a -> a.discontinue(reason, c.operatorId(), ids));
    }

    /**
     * 处理当前类型职责中的操作 {@code change}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param request 接口请求参数，类型为 {@code Object}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @param op 业务处理参数或成员，类型为 {@code String}
     * @param action 业务处理参数或成员，类型为 {@code Consumer<SupplierItemAggregate>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private CommandResult change(long id, int version, Object request, CommandContext c, String op, Consumer<SupplierItemAggregate> action) {
        return executor.execute("supplier:item", c, request, () -> {
            var a = repo.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "供应商商品不存在"));
            c.requireSupplierScope(a.supplierId());
            if (a.version() != version) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "供应商商品版本已变化");
            }
            String before = snapshot(a);
            action.accept(a);
            return persist(a, c, op, before);
        });
    }

    /**
     * 处理当前类型职责中的操作 {@code persist}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param a 业务处理参数或成员，类型为 {@code SupplierItemAggregate}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @param op 业务处理参数或成员，类型为 {@code String}
     * @param before 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private CommandResult persist(SupplierItemAggregate a, CommandContext c, String op, String before) {
        repo.save(a, c.operatorId());
        if (Set.of(ENABLE_SUPPLIER_ITEM, CHANGE_SUPPLY_CONDITION).contains(op)) {
            history.recordCondition(a, op, c.operatorId());
        }
        List<DomainEvent> events = a.pullEvents();
        outbox.saveAll(events);
        audit.save(c, op, "SUPPLIER_ITEM", a.itemId(), Long.toString(a.itemId()), before, snapshot(a));
        String event = events.isEmpty() ? null : events.get(events.size() - 1).eventCode();
        return new CommandResult(a.itemId(), Long.toString(a.itemId()), a.status().code(), a.status().label(), a.version(), event, false);
    }

    /**
     * 校验业务约束 {@code ensureEligible}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     */
    private void ensureEligible(long supplierId, String skuCode) {
        var supplier = masterData.findSupplier(supplierId).orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "供应商主数据快照不存在或尚未同步"));
        if (!supplier.enabled()) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "供应商未启用，不能建立或恢复供货关系");
        }
        var sku = masterData.findSku(skuCode).orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "SKU主数据快照不存在或尚未同步"));
        if (!sku.enabled()) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "SKU已停用，不能建立或恢复供货关系");
        }
        qualifications.assertEligible(supplierId, sku.categoryId());
    }

    /**
     * 处理当前类型职责中的操作 {@code snapshot}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param a 业务处理参数或成员，类型为 {@code SupplierItemAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String snapshot(SupplierItemAggregate a) {
        return "{\"itemId\":%d,\"status\":%d,\"version\":%d}".formatted(a.itemId(), a.status().code(), a.version());
    }

    /**
     * 校验业务约束 {@code validatePage}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     */
    private void validatePage(int pageNo, int pageSize) {
        if (pageNo < 1 || pageSize < 1 || pageSize > VALIDATE_PAGE_VALUE_100) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "分页参数不合法");
        }
    }

    /**
     * 建立供应商供货关系的幂等请求快照。
     *
     * <p>将供应商、SKU、供应商侧编码和供货条件组成不可变输入，供命令执行器计算幂等结果。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private record EnableRequest(
            long supplierId,
            String sku,
            String supplierSku,
            SupplyCondition condition) {
    }

    /**
     * ChangeRequest。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private record ChangeRequest(long id, int version, String supplierSku, SupplyCondition condition) {
    }

    /**
     * StateRequest。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private record StateRequest(long id, int version, String reason) {
    }

    /**
     * 业务常量 {@code VALIDATE_PAGE_VALUE_100}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int VALIDATE_PAGE_VALUE_100 = 100;

    /**
     * 业务常量 {@code CHANGE_SUPPLY_CONDITION}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String CHANGE_SUPPLY_CONDITION = "CHANGE_SUPPLY_CONDITION";

    /**
     * 业务常量 {@code ENABLE_SUPPLIER_ITEM}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String ENABLE_SUPPLIER_ITEM = "ENABLE_SUPPLIER_ITEM";
}
