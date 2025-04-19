package org.scm.oms.adapter.infra.jpa;

import org.scm.oms.adapter.infra.domain.ReturnApplyItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnApplyItemJpaRepository extends JpaRepository<ReturnApplyItem, Long> {
    List<ReturnApplyItem> findByReturnApplyNo(String returnApplyNo);
}
