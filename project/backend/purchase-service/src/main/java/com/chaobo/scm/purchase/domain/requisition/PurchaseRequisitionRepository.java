package com.chaobo.scm.purchase.domain.requisition;

import java.util.Optional;

public interface PurchaseRequisitionRepository {

    Optional<PurchaseRequisitionAggregate> findById(long id);

    Optional<PurchaseRequisitionAggregate> findByNo(String requisitionNo);

    void save(PurchaseRequisitionAggregate aggregate, long operatorId);
}
