package com.chaobo.scm.purchase.domain.inbound;

import java.util.Optional;

public interface InboundTrackingRepository {
    Optional<InboundTrackingAggregate> findByNo(String inboundNo);
    Optional<InboundTrackingAggregate> findByAsnNo(String asnNo);
    void save(InboundTrackingAggregate aggregate, long operatorId);
}
