package org.scm.bdp.service.adapter.infra.jpa;

import org.scm.bdp.service.adapter.infra.domain.LogisticsChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogisticsChannelJpaRepository extends JpaRepository<LogisticsChannel, Long> {
}