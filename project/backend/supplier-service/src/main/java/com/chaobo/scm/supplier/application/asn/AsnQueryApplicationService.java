package com.chaobo.scm.supplier.application.asn;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.supplier.domain.asn.AsnAggregate;
import com.chaobo.scm.supplier.domain.asn.AsnRepository;
import com.chaobo.scm.common.api.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AsnQueryApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class AsnQueryApplicationService {

    /**
     * repository（类型：{@code AsnRepository}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final AsnRepository repository;

    /**
     * readModelPort（类型：{@code AsnReadModelPort}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final AsnReadModelPort readModelPort;

    /**
     * 创建 AsnQueryApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param repository 持久化访问依赖，类型为 {@code AsnRepository}
     * @param readModelPort 应用或外部协作依赖，类型为 {@code AsnReadModelPort}
     */
    public AsnQueryApplicationService(AsnRepository repository, AsnReadModelPort readModelPort) {
        this.repository = repository;
        this.readModelPort = readModelPort;
    }

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param requestedSupplierId 业务或技术标识，类型为 {@code Long}
     * @param supplierScopeId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param keyword 业务处理参数或成员，类型为 {@code String}
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PageResult<AsnSummaryView>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PageResult<AsnSummaryView> page(Long requestedSupplierId, Long supplierScopeId, Integer status, String keyword, int pageNo, int pageSize) {
        if (pageNo < 1 || pageSize < 1 || pageSize > PAGE_VALUE_100) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "分页参数不合法");
        }
        Long effectiveSupplierId = supplierScopeId == null ? requestedSupplierId : supplierScopeId;
        return readModelPort.page(effectiveSupplierId, status, keyword, pageNo, pageSize);
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param asnId 业务或技术标识，类型为 {@code long}
     * @param supplierScopeId 业务或技术标识，类型为 {@code Long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code AsnDetailView}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public AsnDetailView detail(long asnId, Long supplierScopeId) {
        AsnAggregate aggregate = repository.findById(asnId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "ASN 不存在"));
        if (supplierScopeId != null && supplierScopeId != aggregate.supplierId()) {
            // 对越权数据返回 404，避免泄露其他供应商的记录是否存在。
            throw new BusinessException(ErrorCode.NOT_FOUND, "ASN 不存在");
        }
        var shipment = aggregate.shipmentInfo();
        return new AsnDetailView(aggregate.asnId(), aggregate.asnNo(), aggregate.purchaseOrderId(), aggregate.supplierId(), aggregate.warehouseId(), aggregate.estimatedArrivalAt(), aggregate.status().code(), aggregate.status().label(), shipment == null ? null : shipment.shippedAt(), shipment == null ? null : shipment.carrierName(), shipment == null ? null : shipment.trackingNo(), aggregate.cancelReason(), aggregate.version(), aggregate.lines().stream().map(line -> new AsnDetailView.LineView(line.lineId(), line.skuCode(), line.plannedQuantity(), line.receivedQuantity(), line.batchNo(), line.productionDate(), line.expireDate())).toList());
    }

    /**
     * 业务常量 {@code PAGE_VALUE_100}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int PAGE_VALUE_100 = 100;
}
