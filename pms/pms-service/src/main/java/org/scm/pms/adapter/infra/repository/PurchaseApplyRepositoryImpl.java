package org.scm.pms.adapter.infra.repository;

import org.scm.pms.adapter.infra.jpa.PurchaseApplyJpaRepository;
import org.scm.pms.domain.model.PurchaseApplyAgg;
import org.scm.pms.domain.repository.PurchaseApplyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PurchaseApplyRepositoryImpl implements PurchaseApplyRepository {

    @Autowired
    private PurchaseApplyJpaRepository repository;


    @Override
    public void save(PurchaseApplyAgg purchaseApplyAgg) {

    }

    @Override
    public PurchaseApplyAgg findById(Long id) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }
}
