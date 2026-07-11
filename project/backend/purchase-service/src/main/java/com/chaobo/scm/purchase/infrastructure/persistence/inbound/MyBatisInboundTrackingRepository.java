package com.chaobo.scm.purchase.infrastructure.persistence.inbound;

import com.chaobo.scm.purchase.domain.inbound.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MyBatisInboundTrackingRepository implements InboundTrackingRepository {
    private final InboundTrackingMapper mapper;

    public MyBatisInboundTrackingRepository(InboundTrackingMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<InboundTrackingAggregate> findByNo(String inboundNo) {
        return Optional.ofNullable(mapper.findByNo(inboundNo)).map(this::aggregate);
    }

    @Override
    public Optional<InboundTrackingAggregate> findByAsnNo(String asnNo) {
        return Optional.ofNullable(mapper.findByAsnNo(asnNo)).map(this::aggregate);
    }

    @Override
    public void save(InboundTrackingAggregate aggregate, long operatorId) {
        var row = row(aggregate);
        if (mapper.findByNo(aggregate.inboundNo()) == null) {
            mapper.insert(row, operatorId);
        } else {
            mapper.update(row, operatorId);
        }
    }

    private InboundTrackingMapper.Row row(InboundTrackingAggregate a) {
        return new InboundTrackingMapper.Row(a.id(), a.inboundNo(), a.orderNo(), a.asnNo(), a.supplierId(),
                a.purchaseOrgId(), a.warehouseCode(), a.skuCode(), a.notifiedQty(), a.receivedQty(),
                a.qualifiedQty(), a.unqualifiedQty(), a.putawayQty(), a.status().code(), a.exceptionReason(),
                a.version());
    }

    private InboundTrackingAggregate aggregate(InboundTrackingMapper.Row row) {
        return new InboundTrackingAggregate(row.id(), row.inboundNo(), row.orderNo(), row.asnNo(), row.supplierId(),
                row.purchaseOrgId(), row.warehouseCode(), row.skuCode(), row.notifiedQty(), row.receivedQty(),
                row.qualifiedQty(), row.unqualifiedQty(), row.putawayQty(), InboundStatus.of(row.status()),
                row.exceptionReason(), row.version());
    }
}
