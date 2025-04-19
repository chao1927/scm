package org.scm.srm.wms.adapter.infra.repository;

import org.scm.srm.wms.adapter.infra.jpa.SortingOrderJpaRepository;
import org.scm.srm.wms.domain.model.SortingOrderAgg;
import org.scm.srm.wms.domain.repository.SortingOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SortingOrderRepositoryImpl implements SortingOrderRepository {

    @Autowired
    private SortingOrderJpaRepository jpaRepository;

    @Override
    public void save(SortingOrderAgg sortingOrderAgg) {

    }

    @Override
    public SortingOrderAgg findById(Long id) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }
}
