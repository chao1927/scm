package com.chaobo.scm.purchase.infrastructure.persistence.order;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.purchase.application.order.*;
import com.chaobo.scm.purchase.domain.order.PurchaseOrderStatus;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MyBatisPurchaseOrderReadModel implements PurchaseOrderReadModelPort {
    private final PurchaseOrderMapper mapper;
    private final PurchaseOrderQueryMapper queryMapper;

    public MyBatisPurchaseOrderReadModel(PurchaseOrderMapper mapper, PurchaseOrderQueryMapper queryMapper) {
        this.mapper = mapper;
        this.queryMapper = queryMapper;
    }

    @Override
    public PageResult<PurchaseOrderView> page(Long purchaseOrgId, Long supplierId, Integer status, int pageNo, int pageSize) {
        var total = queryMapper.count(purchaseOrgId, supplierId, status);
        var records = queryMapper.page(purchaseOrgId, supplierId, status, (pageNo - 1) * pageSize, pageSize)
                .stream().map(this::view).toList();
        return new PageResult<>(pageNo, pageSize, total, records);
    }

    @Override
    public Optional<PurchaseOrderView> detail(String orderNo) {
        return Optional.ofNullable(mapper.findByNo(orderNo)).map(this::view);
    }

    private PurchaseOrderView view(PurchaseOrderMapper.HeaderRow row) {
        var status = PurchaseOrderStatus.of(row.status());
        var lines = mapper.findLines(row.id()).stream().map(line -> new PurchaseOrderView.Line(line.lineId(),
                line.skuCode(), line.skuName(), line.orderQty(), line.unitPrice(), line.taxRate(),
                line.taxIncludedPrice(), line.requiredDeliveryDate(), line.receivedQty())).toList();
        return new PurchaseOrderView(row.id(), row.orderNo(), row.purchaseType(), row.supplierId(), row.supplierCode(),
                row.supplierName(), row.purchaseOrgId(), row.warehouseCode(), row.currency(), row.totalAmount(),
                row.taxAmount(), row.taxIncludedAmount(), row.status(), status.label(), row.versionNo(), row.version(),
                row.releasedAt(), row.cancelReason(), lines);
    }
}
