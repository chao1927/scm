package org.scm.srm.wms.domain.repository;

import org.scm.common.BaseRepository;
import org.scm.srm.wms.domain.model.WaveOrderAgg;

public interface WaveOrderRepository extends BaseRepository<WaveOrderAgg> {

    WaveOrderAgg findByWaveNo(String waveNo);
}
