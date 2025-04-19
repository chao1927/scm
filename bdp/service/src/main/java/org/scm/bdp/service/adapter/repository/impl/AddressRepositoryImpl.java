package org.scm.bdp.service.adapter.repository.impl;

import org.scm.bdp.service.adapter.infra.jpa.AddressJpaRepository;
import org.scm.bdp.service.domain.model.AddressAgg;
import org.scm.bdp.service.domain.repository.AddressRepository;
import org.scm.common.exception.AddressErrorCode;
import org.scm.common.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AddressRepositoryImpl implements AddressRepository {
    @Autowired
    private AddressJpaRepository addressJpaRepository;
    @Override
    public void save(AddressAgg addressAgg) {
        // TODO: 实现保存逻辑
    }
    @Override
    public AddressAgg findById(Long id) {
        return addressJpaRepository.findById(id)
                .map(AddressAgg::new)
                .orElseThrow(() -> new BizException(AddressErrorCode.ADDRESS_ID_ERROR));
    }

    @Override
    public void deleteById(Long id) {

    }
}