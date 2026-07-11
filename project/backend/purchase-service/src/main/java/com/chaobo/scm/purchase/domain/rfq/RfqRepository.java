package com.chaobo.scm.purchase.domain.rfq;

import java.util.Optional;

public interface RfqRepository {

    Optional<RfqAggregate> findById(long id);

    Optional<RfqAggregate> findByNo(String rfqNo);

    void save(RfqAggregate aggregate, long operatorId);
}
