package org.scm.oms.adapter.infra.jpa;

import org.scm.oms.adapter.infra.domain.ReturnApply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnApplyJpaRepository extends JpaRepository<ReturnApply, Long> {
    ReturnApply findByReturnApplyNo(String returnApplyNo);
}
