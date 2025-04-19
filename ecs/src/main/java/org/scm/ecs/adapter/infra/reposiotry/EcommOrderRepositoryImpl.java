package org.scm.ecs.adapter.infra.reposiotry;

import org.scm.ecs.domain.model.EcommOrderAgg;
import org.scm.ecs.domain.repository.EcommOrderRepository;

public class EcommOrderRepositoryImpl implements EcommOrderRepository {
    @Override
    public void save(EcommOrderAgg ecommOrderAgg) {

    }

    @Override
    public EcommOrderAgg findById(Long id) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }

    @Override
    public EcommOrderAgg findByPlatformOrderNo(String platformOrderNo) {
        return null;
    }
}
