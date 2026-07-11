package com.chaobo.scm.supplier.domain.asn;

import java.util.Optional;
import java.util.List;

public interface AsnRepository {
    Optional<AsnAggregate> findById(long asnId);
    List<AsnAggregate> findByPurchaseOrderId(long purchaseOrderId);

    void save(AsnAggregate aggregate, long operatorId);
}
