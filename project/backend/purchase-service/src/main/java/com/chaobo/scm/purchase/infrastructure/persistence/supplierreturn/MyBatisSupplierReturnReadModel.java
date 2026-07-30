package com.chaobo.scm.purchase.infrastructure.persistence.supplierreturn;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.purchase.application.supplierreturn.*;
import com.chaobo.scm.purchase.domain.supplierreturn.SupplierReturnStatus;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * MyBatisSupplierReturnReadModel。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisSupplierReturnReadModel implements SupplierReturnReadModelPort {

    /**
     * mapper（类型：{@code SupplierReturnMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierReturnMapper mapper;

    /**
     * 创建 MyBatisSupplierReturnReadModel。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code SupplierReturnMapper}
     */
    public MyBatisSupplierReturnReadModel(SupplierReturnMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param purchaseOrgId 业务或技术标识，类型为 {@code Long}
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param warehouseCode 可追踪业务编码，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PageResult<SupplierReturnView>}
     */
    @Override
    public PageResult<SupplierReturnView> page(Long purchaseOrgId, Long supplierId, String warehouseCode, Integer status, int pageNo, int pageSize) {
        var total = mapper.count(purchaseOrgId, supplierId, warehouseCode, status);
        var records = mapper.page(purchaseOrgId, supplierId, warehouseCode, status, (pageNo - 1) * pageSize, pageSize).stream().map(this::view).toList();
        return new PageResult<>(pageNo, pageSize, total, records);
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param returnNo 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Optional<SupplierReturnView>}
     */
    @Override
    public Optional<SupplierReturnView> detail(String returnNo) {
        return Optional.ofNullable(mapper.findByNo(returnNo)).map(this::view);
    }

    /**
     * 处理当前类型职责中的操作 {@code view}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code SupplierReturnMapper.HeaderRow}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierReturnView}
     */
    private SupplierReturnView view(SupplierReturnMapper.HeaderRow row) {
        var status = SupplierReturnStatus.of(row.status());
        var lines = mapper.findLines(row.id()).stream().map(line -> new SupplierReturnView.Line(line.lineId(), line.skuCode(), line.returnQty(), line.returnableQty(), line.reason())).toList();
        return new SupplierReturnView(row.id(), row.returnNo(), row.sourceOrderNo(), row.supplierId(), row.purchaseOrgId(), row.warehouseCode(), row.status(), status.label(), row.rejectReason(), row.version(), lines);
    }
}
