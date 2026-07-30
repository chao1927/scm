package com.chaobo.scm.supplier.application.workbench;

import com.chaobo.scm.supplier.infrastructure.persistence.workbench.SupplierWorkbenchMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;

/**
 * SupplierWorkbenchApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class SupplierWorkbenchApplicationService {

    /**
     * mapper（类型：{@code SupplierWorkbenchMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierWorkbenchMapper mapper;

    /**
     * 创建 SupplierWorkbenchApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code SupplierWorkbenchMapper}
     */
    public SupplierWorkbenchApplicationService(SupplierWorkbenchMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 处理当前类型职责中的操作 {@code summary}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param supplierScopeId 业务或技术标识，类型为 {@code Long}
     * @param recentDays 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierWorkbenchView}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public SupplierWorkbenchView summary(Long supplierId, Long supplierScopeId, int recentDays) {
        if (recentDays < 1 || recentDays > SUMMARY_VALUE_365) {
            throw new com.chaobo.scm.common.error.BusinessException(com.chaobo.scm.common.error.ErrorCode.VALIDATION_FAILED, "统计天数必须在1到365之间");
        }
        var scopedSupplierId = supplierScopeId == null ? supplierId : supplierScopeId;
        var since = OffsetDateTime.now().minusDays(recentDays);
        return new SupplierWorkbenchView(mapper.pendingQuotes(scopedSupplierId), mapper.pendingPurchaseOrderConfirms(scopedSupplierId), mapper.pendingAsns(scopedSupplierId), mapper.pendingReconciliations(scopedSupplierId), mapper.pendingRectifications(scopedSupplierId), mapper.openWarnings(scopedSupplierId), mapper.failedEvents(), mapper.openReturns(scopedSupplierId), mapper.latestScore(scopedSupplierId, since), OffsetDateTime.now(), mapper.todoGroups(scopedSupplierId), mapper.warningGroups(scopedSupplierId));
    }

    /**
     * 业务常量 {@code SUMMARY_VALUE_365}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int SUMMARY_VALUE_365 = 365;
}
