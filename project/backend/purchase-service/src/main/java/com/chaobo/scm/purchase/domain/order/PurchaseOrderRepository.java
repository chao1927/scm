package com.chaobo.scm.purchase.domain.order;

import java.util.Optional;

public interface PurchaseOrderRepository {

    Optional<PurchaseOrderAggregate> findByNo(String orderNo);

    void save(PurchaseOrderAggregate aggregate, long operatorId);
}
