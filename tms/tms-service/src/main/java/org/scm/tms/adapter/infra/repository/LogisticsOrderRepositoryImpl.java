package org.scm.tms.adapter.infra.repository;

import org.scm.tms.domain.model.LogisticsOrderAgg;
import org.scm.tms.domain.repository.LogisticsOrderRepository;

public class LogisticsOrderRepositoryImpl implements LogisticsOrderRepository {
    @Override
    public void save(LogisticsOrderAgg logisticsOrderAgg) {

    }

    @Override
    public LogisticsOrderAgg findById(Long id) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }

    @Override
    public LogisticsOrderAgg findByLogisticsNo(String logisticsNo) {
        return null;
    }
}
