package org.scm.srm.wms.adapter.infra.repository;

import org.scm.srm.wms.domain.model.WaveOrderAgg;
import org.scm.srm.wms.domain.repository.WaveOrderRepository;
import org.springframework.stereotype.Repository;

@Repository
public class WaveOrderRepositoryImpl implements WaveOrderRepository {

    @Override
    public void save(WaveOrderAgg waveOrderAgg) {

    }

    @Override
    public WaveOrderAgg findById(Long id) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }

    @Override
    public WaveOrderAgg findByWaveNo(String waveNo) {
        return null;
    }
}