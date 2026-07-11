package com.chaobo.scm.purchase.infrastructure.persistence.supplierreturn;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.purchase.application.supplierreturn.*;
import com.chaobo.scm.purchase.domain.supplierreturn.SupplierReturnStatus;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MyBatisSupplierReturnReadModel implements SupplierReturnReadModelPort {
    private final SupplierReturnMapper mapper;

    public MyBatisSupplierReturnReadModel(SupplierReturnMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public PageResult<SupplierReturnView> page(Long purchaseOrgId, Long supplierId, String warehouseCode, Integer status,
                                               int pageNo, int pageSize) {
        var total = mapper.count(purchaseOrgId, supplierId, warehouseCode, status);
        var records = mapper.page(purchaseOrgId, supplierId, warehouseCode, status, (pageNo - 1) * pageSize, pageSize)
                .stream().map(this::view).toList();
        return new PageResult<>(pageNo, pageSize, total, records);
    }

    @Override
    public Optional<SupplierReturnView> detail(String returnNo) {
        return Optional.ofNullable(mapper.findByNo(returnNo)).map(this::view);
    }

    private SupplierReturnView view(SupplierReturnMapper.HeaderRow row) {
        var status = SupplierReturnStatus.of(row.status());
        var lines = mapper.findLines(row.id()).stream().map(line -> new SupplierReturnView.Line(line.lineId(),
                line.skuCode(), line.returnQty(), line.returnableQty(), line.reason())).toList();
        return new SupplierReturnView(row.id(), row.returnNo(), row.sourceOrderNo(), row.supplierId(),
                row.purchaseOrgId(), row.warehouseCode(), row.status(), status.label(), row.rejectReason(),
                row.version(), lines);
    }
}
