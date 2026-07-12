package com.chaobo.scm.wms.infrastructure.persistence.inbound;

import com.chaobo.scm.wms.domain.inbound.InboundOrderAggregate;
import com.chaobo.scm.wms.domain.inbound.InboundOrderRepository;
import com.chaobo.scm.wms.domain.inbound.InboundOrderStatus;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MyBatisInboundOrderRepository implements InboundOrderRepository {
    private final InboundOrderMapper mapper;

    public MyBatisInboundOrderRepository(InboundOrderMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<InboundOrderAggregate> findById(long id) {
        return Optional.ofNullable(mapper.findById(id)).map(this::toAggregate);
    }

    @Override
    public Optional<InboundOrderAggregate> findBySource(String sourceType, String sourceNo, long warehouseId) {
        return Optional.ofNullable(mapper.findBySource(sourceType, sourceNo, warehouseId)).map(this::toAggregate);
    }

    @Override
    public void save(InboundOrderAggregate order, long operatorId) {
        if (mapper.findById(order.id()) == null) {
            mapper.insert(order.id(), order.inboundNo(), order.sourceType(), order.sourceNo(), order.warehouseId(),
                    order.status().code(), order.expectedArrivalAt(), order.cancelReason(), order.version(), operatorId);
            return;
        }
        mapper.update(order.id(), order.status().code(), order.cancelReason(), order.version(), operatorId);
    }

    private InboundOrderAggregate toAggregate(InboundOrderMapper.Row row) {
        return new InboundOrderAggregate(row.id(), row.inboundNo(), row.sourceType(), row.sourceNo(), row.warehouseId(),
                InboundOrderStatus.of(row.status()), row.expectedArrivalAt(), row.cancelReason(), row.version());
    }
}
