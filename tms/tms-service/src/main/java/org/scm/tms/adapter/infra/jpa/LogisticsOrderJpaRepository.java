package org.scm.tms.adapter.infra.jpa;

import org.scm.tms.adapter.infra.domain.LogisticsOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogisticsOrderJpaRepository extends JpaRepository<LogisticsOrder, Long> {
    LogisticsOrder findByLogisticsNo(String logisticsNo);
}
