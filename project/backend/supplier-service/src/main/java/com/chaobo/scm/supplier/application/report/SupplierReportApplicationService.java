package com.chaobo.scm.supplier.application.report;

import com.chaobo.scm.supplier.infrastructure.persistence.report.SupplierReportMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SupplierReportApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class SupplierReportApplicationService {

    /**
     * mapper（类型：{@code SupplierReportMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierReportMapper mapper;

    /**
     * 创建 SupplierReportApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code SupplierReportMapper}
     */
    public SupplierReportApplicationService(SupplierReportMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 处理当前类型职责中的操作 {@code fulfillment}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param supplierScopeId 业务或技术标识，类型为 {@code Long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierReportViews.Fulfillment}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public SupplierReportViews.Fulfillment fulfillment(Long supplierId, Long supplierScopeId) {
        return mapper.fulfillment(supplierScopeId == null ? supplierId : supplierScopeId);
    }

    /**
     * 处理当前类型职责中的操作 {@code exceptions}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param supplierScopeId 业务或技术标识，类型为 {@code Long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierReportViews.ExceptionOverview}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public SupplierReportViews.ExceptionOverview exceptions(Long supplierId, Long supplierScopeId) {
        return mapper.exceptions(supplierScopeId == null ? supplierId : supplierScopeId);
    }
}
