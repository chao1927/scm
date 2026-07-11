package com.chaobo.scm.purchase.infrastructure.persistence.supplierreturn;

import com.chaobo.scm.purchase.domain.supplierreturn.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MyBatisSupplierReturnRepository implements SupplierReturnRepository {
    private final SupplierReturnMapper mapper;

    public MyBatisSupplierReturnRepository(SupplierReturnMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<SupplierReturnAggregate> findByNo(String returnNo) {
        return Optional.ofNullable(mapper.findByNo(returnNo)).map(this::aggregate);
    }

    @Override
    public void save(SupplierReturnAggregate aggregate, long operatorId) {
        var existed = mapper.findByNo(aggregate.returnNo()) != null;
        if (existed) {
            mapper.updateHeader(aggregate.id(), aggregate.status().code(), aggregate.rejectReason(), aggregate.version(),
                    operatorId);
            mapper.deleteLines(aggregate.id());
        } else {
            mapper.insertHeader(aggregate.id(), aggregate.returnNo(), aggregate.sourceOrderNo(), aggregate.supplierId(),
                    aggregate.purchaseOrgId(), aggregate.warehouseCode(), aggregate.status().code(),
                    aggregate.rejectReason(), aggregate.version(), operatorId);
        }
        for (SupplierReturnLine line : aggregate.lines()) {
            mapper.insertLine(new SupplierReturnMapper.LineRow(line.lineId(), aggregate.id(), line.skuCode(),
                    line.returnQty(), line.returnableQty(), line.reason()));
        }
    }

    private SupplierReturnAggregate aggregate(SupplierReturnMapper.HeaderRow row) {
        var lines = mapper.findLines(row.id()).stream().map(line -> new SupplierReturnLine(line.lineId(),
                line.skuCode(), line.returnQty(), line.returnableQty(), line.reason())).toList();
        return new SupplierReturnAggregate(row.id(), row.returnNo(), row.sourceOrderNo(), row.supplierId(),
                row.purchaseOrgId(), row.warehouseCode(), SupplierReturnStatus.of(row.status()), row.rejectReason(),
                row.version(), lines);
    }
}
