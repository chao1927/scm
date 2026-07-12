package com.chaobo.scm.wms.domain.inbound;

import java.util.Optional;

public interface InboundOrderRepository {
    Optional<InboundOrderAggregate> findById(long id);
    Optional<InboundOrderAggregate> findBySource(String sourceType, String sourceNo, long warehouseId);
    void save(InboundOrderAggregate order, long operatorId);
}
