package org.scm.pms.adapter.infra.jpa;

import org.scm.pms.adapter.infra.domain.PurchaseApplyItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseApplyItemJpaRepository extends JpaRepository<PurchaseApplyItem, Long> {
    List<PurchaseApplyItem> findByApplyNo(String applyNo);
}
