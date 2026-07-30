package com.chaobo.scm.purchase.infrastructure.persistence.supplierreturn;

import com.chaobo.scm.purchase.domain.supplierreturn.*;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * MyBatisSupplierReturnRepository。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisSupplierReturnRepository implements SupplierReturnRepository {

    /**
     * mapper（类型：{@code SupplierReturnMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierReturnMapper mapper;

    /**
     * 创建 MyBatisSupplierReturnRepository。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code SupplierReturnMapper}
     */
    public MyBatisSupplierReturnRepository(SupplierReturnMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 查询并返回 {@code findByNo}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param returnNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code Optional<SupplierReturnAggregate>}
     */
    @Override
    public Optional<SupplierReturnAggregate> findByNo(String returnNo) {
        return Optional.ofNullable(mapper.findByNo(returnNo)).map(this::aggregate);
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param aggregate 业务处理参数或成员，类型为 {@code SupplierReturnAggregate}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
    @Override
    public void save(SupplierReturnAggregate aggregate, long operatorId) {
        var existed = mapper.findByNo(aggregate.returnNo()) != null;
        if (existed) {
            mapper.updateHeader(aggregate.id(), aggregate.status().code(), aggregate.rejectReason(), aggregate.version(), operatorId);
            mapper.deleteLines(aggregate.id());
        } else {
            mapper.insertHeader(aggregate.id(), aggregate.returnNo(), aggregate.sourceOrderNo(), aggregate.supplierId(), aggregate.purchaseOrgId(), aggregate.warehouseCode(), aggregate.status().code(), aggregate.rejectReason(), aggregate.version(), operatorId);
        }
        for (SupplierReturnLine line : aggregate.lines()) {
            mapper.insertLine(new SupplierReturnMapper.LineRow(line.lineId(), aggregate.id(), line.skuCode(), line.returnQty(), line.returnableQty(), line.reason()));
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code aggregate}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code SupplierReturnMapper.HeaderRow}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierReturnAggregate}
     */
    private SupplierReturnAggregate aggregate(SupplierReturnMapper.HeaderRow row) {
        var lines = mapper.findLines(row.id()).stream().map(line -> new SupplierReturnLine(line.lineId(), line.skuCode(), line.returnQty(), line.returnableQty(), line.reason())).toList();
        return new SupplierReturnAggregate(row.id(), row.returnNo(), row.sourceOrderNo(), row.supplierId(), row.purchaseOrgId(), row.warehouseCode(), SupplierReturnStatus.of(row.status()), row.rejectReason(), row.version(), lines);
    }
}
