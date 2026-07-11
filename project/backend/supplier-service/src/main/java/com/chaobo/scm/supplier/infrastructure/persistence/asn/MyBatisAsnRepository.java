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

@Repository
public class MyBatisAsnRepository implements AsnRepository {
    private final AsnMapper mapper;

    public MyBatisAsnRepository(AsnMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<AsnAggregate> findById(long asnId) {
        AsnRow row = mapper.findById(asnId);
        if (row == null) {
            return Optional.empty();
        }
        List<AsnLine> lines = mapper.findLines(asnId).stream()
                .map(line -> new AsnLine(line.asnLineId(), line.skuCode(), line.plannedQty(),
                        line.receivedQty(), line.batchNo(), line.productionDate(), line.expireDate()))
                .toList();
        ShipmentInfo shipment = row.shipAt() == null ? null
                : new ShipmentInfo(row.shipAt(), row.carrierName(), row.trackingNo());
        return Optional.of(AsnAggregate.rehydrate(row.asnId(), row.asnNo(), row.purchaseOrderId(),
                row.supplierId(), row.warehouseId(), row.eta(), lines,
                AsnStatus.fromCode(row.asnStatus()), shipment, row.cancelReason(), row.version()));
    }

    @Override
    public List<AsnAggregate> findByPurchaseOrderId(long purchaseOrderId) {
        return mapper.findIdsByPurchaseOrderId(purchaseOrderId).stream()
                .map(this::findById).flatMap(Optional::stream).toList();
    }

    @Override
    public void save(AsnAggregate aggregate, long operatorId) {
        AsnRow row = toRow(aggregate);
        if (mapper.findById(aggregate.asnId()) == null) {
            mapper.insert(row, operatorId);
            aggregate.lines().forEach(line -> mapper.insertLine(new AsnLineRow(line.lineId(),
                    aggregate.asnId(), line.skuCode(), line.plannedQuantity(), line.receivedQuantity(),
                    line.batchNo(), line.productionDate(), line.expireDate()), operatorId));
            return;
        }
        int updated = mapper.update(row, aggregate.version() - 1, operatorId);
        if (updated != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "ASN 已被其他操作更新，请刷新后重试");
        }
    }

    private AsnRow toRow(AsnAggregate aggregate) {
        ShipmentInfo shipment = aggregate.shipmentInfo();
        return new AsnRow(aggregate.asnId(), aggregate.asnNo(), aggregate.purchaseOrderId(),
                aggregate.supplierId(), aggregate.warehouseId(), aggregate.estimatedArrivalAt(),
                shipment == null ? null : shipment.shippedAt(),
                shipment == null ? null : shipment.carrierName(),
                shipment == null ? null : shipment.trackingNo(), aggregate.status().code(),
                aggregate.cancelReason(), aggregate.version());
    }
}
