package org.scm.ecs.adapter.infra.jpa;

import org.scm.ecs.adapter.infra.domain.EcommOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EcommOrderJpaRepository extends JpaRepository<EcommOrder, Long> {
    EcommOrder findByPlatformOrderNo(String platformOrderNo);
}
