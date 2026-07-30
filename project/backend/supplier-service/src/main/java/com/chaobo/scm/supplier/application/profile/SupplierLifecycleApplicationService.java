package com.chaobo.scm.supplier.application.profile;

import com.chaobo.scm.common.integration.MasterDataCollaborationApi;
import com.chaobo.scm.supplier.application.integration.IntegrationCommandEnqueuer;
import com.chaobo.scm.supplier.application.masterdata.MasterDataSnapshotPort;
import com.chaobo.scm.supplier.application.shared.CommandContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SupplierLifecycleApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class SupplierLifecycleApplicationService {

    /**
     * snapshots（类型：{@code MasterDataSnapshotPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final MasterDataSnapshotPort snapshots;

    /**
     * integrations（类型：{@code IntegrationCommandEnqueuer}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final IntegrationCommandEnqueuer integrations;

    /**
     * 创建 SupplierLifecycleApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param snapshots 业务处理参数或成员，类型为 {@code MasterDataSnapshotPort}
     * @param integrations 业务处理参数或成员，类型为 {@code IntegrationCommandEnqueuer}
     */
    public SupplierLifecycleApplicationService(MasterDataSnapshotPort snapshots, IntegrationCommandEnqueuer integrations) {
        this.snapshots = snapshots;
        this.integrations = integrations;
    }

    /**
     * 处理当前类型职责中的操作 {@code change}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param targetStatus 生命周期状态，类型为 {@code int}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     */
    @Transactional(rollbackFor = Exception.class)
    public void change(long supplierId, int targetStatus, String reason, CommandContext context) {
        context.requirePermission("supplier:lifecycle:manage");
        context.requireSupplierScope(supplierId);
        var supplier = snapshots.findSupplier(supplierId).orElseThrow(() -> new com.chaobo.scm.common.error.BusinessException(com.chaobo.scm.common.error.ErrorCode.NOT_FOUND, "供应商不存在"));
        if (targetStatus < CHANGE_VALUE_3 || targetStatus > CHANGE_VALUE_5) {
            throw new com.chaobo.scm.common.error.BusinessException(com.chaobo.scm.common.error.ErrorCode.VALIDATION_FAILED, "目标状态必须为启用、冻结或停用");
        }
        if (reason == null || reason.isBlank()) {
            throw new com.chaobo.scm.common.error.BusinessException(com.chaobo.scm.common.error.ErrorCode.VALIDATION_FAILED, "状态变更原因不能为空");
        }
        integrations.enqueue("MDM_CHANGE_SUPPLIER_STATUS", "SUPPLIER", supplierId, (int) supplier.sourceVersion(), "MDM", new MasterDataCollaborationApi.ChangeSupplierStatusCommand("MDM-STATUS-" + supplierId + "-" + supplier.sourceVersion() + "-" + targetStatus, supplierId, targetStatus, reason));
    }

    /**
     * 业务常量 {@code CHANGE_VALUE_3}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int CHANGE_VALUE_3 = 3;

    /**
     * 业务常量 {@code CHANGE_VALUE_5}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int CHANGE_VALUE_5 = 5;
}
