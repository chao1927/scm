package com.chaobo.scm.supplier.infrastructure.persistence.asn;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.supplier.domain.asn.AsnAggregate;
import com.chaobo.scm.supplier.domain.asn.AsnLine;
import com.chaobo.scm.supplier.domain.asn.AsnRepository;
import com.chaobo.scm.supplier.domain.asn.AsnStatus;
import com.chaobo.scm.supplier.domain.asn.ShipmentInfo;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * MyBatisAsnRepository。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisAsnRepository implements AsnRepository {

    /**
     * mapper（类型：{@code AsnMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final AsnMapper mapper;

    /**
     * 创建 MyBatisAsnRepository。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code AsnMapper}
     */
    public MyBatisAsnRepository(AsnMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 查询并返回 {@code findById}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param asnId 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Optional<AsnAggregate>}
     */
    @Override
    public Optional<AsnAggregate> findById(long asnId) {
        AsnRow row = mapper.findById(asnId);
        if (row == null) {
            return Optional.empty();
        }
        List<AsnLine> lines = mapper.findLines(asnId).stream().map(line -> new AsnLine(line.asnLineId(), line.skuCode(), line.plannedQty(), line.receivedQty(), line.batchNo(), line.productionDate(), line.expireDate())).toList();
        ShipmentInfo shipment = row.shipAt() == null ? null : new ShipmentInfo(row.shipAt(), row.carrierName(), row.trackingNo());
        return Optional.of(AsnAggregate.rehydrate(row.asnId(), row.asnNo(), row.purchaseOrderId(), row.supplierId(), row.warehouseId(), row.eta(), lines, AsnStatus.fromCode(row.asnStatus()), shipment, row.cancelReason(), row.version()));
    }

    /**
     * 查询并返回 {@code findByPurchaseOrderId}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param purchaseOrderId 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code List<AsnAggregate>}
     */
    @Override
    public List<AsnAggregate> findByPurchaseOrderId(long purchaseOrderId) {
        return mapper.findIdsByPurchaseOrderId(purchaseOrderId).stream().map(this::findById).flatMap(Optional::stream).toList();
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param aggregate 业务处理参数或成员，类型为 {@code AsnAggregate}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
    @Override
    public void save(AsnAggregate aggregate, long operatorId) {
        AsnRow row = toRow(aggregate);
        if (mapper.findById(aggregate.asnId()) == null) {
            mapper.insert(row, operatorId);
            aggregate.lines().forEach(line -> mapper.insertLine(new AsnLineRow(line.lineId(), aggregate.asnId(), line.skuCode(), line.plannedQuantity(), line.receivedQuantity(), line.batchNo(), line.productionDate(), line.expireDate()), operatorId));
            return;
        }
        int updated = mapper.update(row, aggregate.version() - 1, operatorId);
        if (updated != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "ASN 已被其他操作更新，请刷新后重试");
        }
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code AsnAggregate}
     * @return 转换数据模型的结果，类型为 {@code AsnRow}
     */
    private AsnRow toRow(AsnAggregate aggregate) {
        ShipmentInfo shipment = aggregate.shipmentInfo();
        return new AsnRow(aggregate.asnId(), aggregate.asnNo(), aggregate.purchaseOrderId(), aggregate.supplierId(), aggregate.warehouseId(), aggregate.estimatedArrivalAt(), shipment == null ? null : shipment.shippedAt(), shipment == null ? null : shipment.carrierName(), shipment == null ? null : shipment.trackingNo(), aggregate.status().code(), aggregate.cancelReason(), aggregate.version());
    }
}
