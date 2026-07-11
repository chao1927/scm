package com.chaobo.scm.purchase.domain.orderchange;

import java.util.Optional;

public interface PurchaseOrderChangeRepository {

    Optional<PurchaseOrderChangeAggregate> findByNo(String changeNo);

    void save(PurchaseOrderChangeAggregate aggregate, long operatorId);
}
