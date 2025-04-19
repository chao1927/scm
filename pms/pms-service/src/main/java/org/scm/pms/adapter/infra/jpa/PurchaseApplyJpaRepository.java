package org.scm.pms.adapter.infra.jpa;

import org.scm.pms.adapter.infra.domain.PurchaseApply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseApplyJpaRepository extends JpaRepository<PurchaseApply, Long> {
    PurchaseApply findByApplyNo(String applyNo);
}
