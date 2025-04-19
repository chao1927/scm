package org.scm.srm.wms.adapter.infra.jpa;

import org.scm.srm.wms.adapter.infra.domain.WaveOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WaveOrderJpaRepository extends JpaRepository<WaveOrder, Long> {
    WaveOrder findByWaveNo(String waveNo);
}
