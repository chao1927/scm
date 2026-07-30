package com.chaobo.scm.purchase.application.supplierconfirm;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.application.order.PurchaseOrderApplicationService;
import com.chaobo.scm.purchase.application.shared.CommandContext;
import com.chaobo.scm.purchase.infrastructure.persistence.supplierconfirm.SupplierConfirmMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SupplierConfirmApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class SupplierConfirmApplicationService {

    /**
     * PENDING（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    private static final int PENDING = 1;

    /**
     * ACCEPTED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    private static final int ACCEPTED = 2;

    /**
     * RENEGOTIATING（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    private static final int RENEGOTIATING = 3;

    /**
     * CANCELLED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    private static final int CANCELLED = 4;

    /**
     * mapper（类型：{@code SupplierConfirmMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierConfirmMapper mapper;

    /**
     * orders（类型：{@code PurchaseOrderApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final PurchaseOrderApplicationService orders;

    /**
     * 创建 SupplierConfirmApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code SupplierConfirmMapper}
     * @param orders 业务处理参数或成员，类型为 {@code PurchaseOrderApplicationService}
     */
    public SupplierConfirmApplicationService(SupplierConfirmMapper mapper, PurchaseOrderApplicationService orders) {
        this.mapper = mapper;
        this.orders = orders;
    }

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param purchaseOrgId 业务或技术标识，类型为 {@code Long}
     * @param scope 业务处理参数或成员，类型为 {@code Long}
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param processedStatus 生命周期状态，类型为 {@code Integer}
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PageResult<SupplierConfirmView>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PageResult<SupplierConfirmView> page(Long purchaseOrgId, Long scope, String orderNo, Long supplierId, Integer processedStatus, int pageNo, int pageSize, CommandContext context) {
        context.requirePermission("purchase:supplier_confirm:read");
        if (pageNo < 1 || pageSize < 1 || pageSize > PAGE_VALUE_100) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "分页参数不合法");
        }
        var org = scope == null ? purchaseOrgId : scope;
        return new PageResult<>(pageNo, pageSize, mapper.count(org, orderNo, supplierId, processedStatus), mapper.page(org, orderNo, supplierId, processedStatus, (pageNo - 1) * pageSize, pageSize).stream().map(this::view).toList());
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param confirmId 业务或技术标识，类型为 {@code long}
     * @param scope 业务处理参数或成员，类型为 {@code Long}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierConfirmView}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public SupplierConfirmView detail(long confirmId, Long scope, CommandContext context) {
        context.requirePermission("purchase:supplier_confirm:read");
        var row = load(confirmId);
        requireScope(row, scope);
        return view(row);
    }

    /**
     * 处理当前类型职责中的操作 {@code acceptDifference}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param confirmId 业务或技术标识，类型为 {@code long}
     * @param command 用例输入命令，类型为 {@code SupplierConfirmCommands.Process}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     */
    @Transactional(rollbackFor = Exception.class)
    public void acceptDifference(long confirmId, SupplierConfirmCommands.Process command, CommandContext context) {
        context.requirePermission("purchase:supplier_confirm:accept_diff");
        var row = pendingDifference(confirmId, command.version(), context);
        orders.acceptSupplierDifference(row.orderNo(), command.comment(), context);
        complete(row, ACCEPTED, command.comment(), context);
    }

    /**
     * 处理当前类型职责中的操作 {@code renegotiate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param confirmId 业务或技术标识，类型为 {@code long}
     * @param command 用例输入命令，类型为 {@code SupplierConfirmCommands.Renegotiate}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     */
    @Transactional(rollbackFor = Exception.class)
    public void renegotiate(long confirmId, SupplierConfirmCommands.Renegotiate command, CommandContext context) {
        context.requirePermission("purchase:supplier_confirm:renegotiate");
        var row = pendingResponse(confirmId, command.version(), context);
        orders.restartSupplierNegotiation(row.orderNo(), command.requirement(), context);
        complete(row, RENEGOTIATING, command.comment(), context);
    }

    /**
     * 执行命令 {@code cancelOrder}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param confirmId 业务或技术标识，类型为 {@code long}
     * @param command 用例输入命令，类型为 {@code SupplierConfirmCommands.CancelOrder}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(long confirmId, SupplierConfirmCommands.CancelOrder command, CommandContext context) {
        context.requirePermission("purchase:supplier_confirm:cancel_order");
        var row = pendingResponse(confirmId, command.version(), context);
        orders.cancelFromSupplierResponse(row.orderNo(), command.reason(), context);
        complete(row, CANCELLED, command.reason(), context);
    }

    /**
     * 处理当前类型职责中的操作 {@code pendingDifference}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param confirmId 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierConfirmMapper.Row}
     */
    private SupplierConfirmMapper.Row pendingDifference(long confirmId, int version, CommandContext context) {
        var row = pendingResponse(confirmId, version, context);
        if (!row.confirmStatus().contains(DIFFERENCE)) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "只有供应商差异反馈可以接受差异");
        }
        return row;
    }

    /**
     * 处理当前类型职责中的操作 {@code pendingResponse}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param confirmId 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierConfirmMapper.Row}
     */
    private SupplierConfirmMapper.Row pendingResponse(long confirmId, int version, CommandContext context) {
        var row = load(confirmId);
        requireScope(row, context.purchaseOrgScope());
        if (row.processedStatus() != PENDING || row.version() != version) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "供应商确认记录已被处理");
        }
        return row;
    }

    /**
     * 执行命令 {@code complete}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code SupplierConfirmMapper.Row}
     * @param status 生命周期状态，类型为 {@code int}
     * @param comment 业务处理参数或成员，类型为 {@code String}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     */
    private void complete(SupplierConfirmMapper.Row row, int status, String comment, CommandContext context) {
        if (mapper.complete(row.confirmId(), row.version(), status, comment, context.operatorId()) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "供应商确认记录已被其他人处理");
        }
    }

    /**
     * 查询并返回 {@code load}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param confirmId 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code SupplierConfirmMapper.Row}
     */
    private SupplierConfirmMapper.Row load(long confirmId) {
        var row = mapper.findById(confirmId);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "供应商确认记录不存在");
        }
        return row;
    }

    /**
     * 查询并返回 {@code requireScope}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param row 业务处理参数或成员，类型为 {@code SupplierConfirmMapper.Row}
     * @param scope 业务处理参数或成员，类型为 {@code Long}
     */
    private static void requireScope(SupplierConfirmMapper.Row row, Long scope) {
        if (scope != null && scope != row.purchaseOrgId()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "供应商确认记录不存在");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code view}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code SupplierConfirmMapper.Row}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierConfirmView}
     */
    private SupplierConfirmView view(SupplierConfirmMapper.Row row) {
        return new SupplierConfirmView(row.confirmId(), row.eventCode(), row.orderNo(), row.supplierId(), row.confirmStatus(), row.reason(), row.sourceVersion(), row.processedStatus(), statusName(row.processedStatus()), row.processComment(), row.purchaseOrgId(), row.version(), row.occurredAt(), row.processedAt(), row.payloadJson());
    }

    /**
     * 处理当前类型职责中的操作 {@code statusName}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param status 生命周期状态，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String statusName(int status) {
        return switch(status) {
            case PENDING ->
                "待处理";
            case ACCEPTED ->
                "已接受差异";
            case RENEGOTIATING ->
                "协商中";
            case CANCELLED ->
                "已取消订单";
            default ->
                "未知";
        };
    }

    /**
     * 业务常量 {@code DIFFERENCE}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String DIFFERENCE = "Difference";

    /**
     * 业务常量 {@code PAGE_VALUE_100}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int PAGE_VALUE_100 = 100;
}
