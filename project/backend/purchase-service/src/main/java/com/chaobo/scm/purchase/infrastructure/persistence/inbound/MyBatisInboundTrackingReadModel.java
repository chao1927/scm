package com.chaobo.scm.purchase.infrastructure.persistence.inbound;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.purchase.application.inbound.*;
import com.chaobo.scm.purchase.domain.inbound.InboundStatus;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MyBatisInboundTrackingReadModel implements InboundTrackingReadModelPort {
    private final InboundTrackingMapper mapper;

    public MyBatisInboundTrackingReadModel(InboundTrackingMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public PageResult<InboundTrackingView> page(Long purchaseOrgId, String orderNo, String asnNo, String warehouseCode,
                                                Integer status, int pageNo, int pageSize) {
        var total = mapper.count(purchaseOrgId, orderNo, asnNo, warehouseCode, status);
        var records = mapper.page(purchaseOrgId, orderNo, asnNo, warehouseCode, status, (pageNo - 1) * pageSize, pageSize)
                .stream().map(this::view).toList();
        return new PageResult<>(pageNo, pageSize, total, records);
    }

    @Override
    public Optional<InboundTrackingView> detail(String inboundNo) {
        return Optional.ofNullable(mapper.findByNo(inboundNo)).map(this::view);
    }

    private InboundTrackingView view(InboundTrackingMapper.Row row) {
        var status = InboundStatus.of(row.status());
        return new InboundTrackingView(row.id(), row.inboundNo(), row.orderNo(), row.asnNo(), row.supplierId(),
                row.purchaseOrgId(), row.warehouseCode(), row.skuCode(), row.notifiedQty(), row.receivedQty(),
                row.qualifiedQty(), row.unqualifiedQty(), row.putawayQty(), row.status(), status.label(),
                row.exceptionReason(), row.version(), true);
    }
}
