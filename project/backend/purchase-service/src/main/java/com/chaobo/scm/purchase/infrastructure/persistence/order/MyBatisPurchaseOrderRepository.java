package com.chaobo.scm.purchase.infrastructure.persistence.order;

import com.chaobo.scm.purchase.domain.order.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MyBatisPurchaseOrderRepository implements PurchaseOrderRepository {
    private final PurchaseOrderMapper mapper;

    public MyBatisPurchaseOrderRepository(PurchaseOrderMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<PurchaseOrderAggregate> findByNo(String orderNo) {
        return Optional.ofNullable(mapper.findByNo(orderNo)).map(this::aggregate);
    }

    @Override
    public void save(PurchaseOrderAggregate order, long operatorId) {
        var existed = mapper.findByNo(order.orderNo()) != null;
        if (existed) {
            mapper.updateHeader(order.id(), order.totalAmount(), order.taxAmount(), order.taxIncludedAmount(),
                    order.status().code(), order.versionNo(), order.version(), order.releasedAt(), order.cancelReason(),
                    operatorId);
            mapper.deleteLines(order.id());
        } else {
            mapper.insertHeader(order.id(), order.orderNo(), order.purchaseType(), order.supplierId(),
                    order.supplierCode(), order.supplierName(), order.purchaseOrgId(), order.warehouseCode(),
                    order.currency(), order.totalAmount(), order.taxAmount(), order.taxIncludedAmount(),
                    order.status().code(), order.versionNo(), order.version(), order.releasedAt(), order.cancelReason(),
                    operatorId);
        }
        for (PurchaseOrderLine line : order.lines()) {
            mapper.insertLine(new PurchaseOrderMapper.LineRow(line.lineId(), order.id(), line.skuCode(), line.skuName(),
                    line.orderQty(), line.unitPrice(), line.taxRate(), line.taxIncludedPrice(),
                    line.requiredDeliveryDate(), line.receivedQty()));
        }
    }

    private PurchaseOrderAggregate aggregate(PurchaseOrderMapper.HeaderRow row) {
        var lines = mapper.findLines(row.id()).stream().map(line -> new PurchaseOrderLine(line.lineId(),
                line.skuCode(), line.skuName(), line.orderQty(), line.unitPrice(), line.taxRate(),
                line.taxIncludedPrice(), line.requiredDeliveryDate(), line.receivedQty())).toList();
        return new PurchaseOrderAggregate(row.id(), row.orderNo(), row.purchaseType(), row.supplierId(),
                row.supplierCode(), row.supplierName(), row.purchaseOrgId(), row.warehouseCode(), row.currency(),
                PurchaseOrderStatus.of(row.status()), row.versionNo(), row.version(), row.releasedAt(),
                row.cancelReason(), lines);
    }
}
