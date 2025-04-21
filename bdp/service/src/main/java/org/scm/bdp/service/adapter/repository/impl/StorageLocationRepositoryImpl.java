package org.scm.bdp.service.adapter.repository.impl;

import org.scm.bdp.service.adapter.infra.jpa.StorageLocationJpaRepository;
import org.scm.bdp.service.domain.model.StorageLocationAgg;
import org.scm.bdp.service.domain.repository.StorageLocationRepository;
import org.scm.common.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import  org.scm.bdp.service._share.enums.errorcode.WarehouseErrorCode;

@Component
public class StorageLocationRepositoryImpl implements StorageLocationRepository {

    @Autowired
    private StorageLocationJpaRepository storageLocationJpaRepository;

    @Override
    public void save(StorageLocationAgg storageLocationAgg) {
        storageLocationJpaRepository.save(storageLocationAgg.location());
    }

    @Override
    public StorageLocationAgg findById(Long id) {
        return storageLocationJpaRepository.findById(id)
                .map(StorageLocationAgg::new)
                .orElseThrow(() -> new BizException(WarehouseErrorCode.STORAGE_LOCATION_NOT_FOUND));
    }

    @Override
    public void deleteById(Long id) {
        storageLocationJpaRepository.deleteById(id);
    }

    @Override
    public void checkCodeExist(String code) {
        storageLocationJpaRepository.findByCode(code).ifPresent(storageLocation -> {
            throw new BizException(WarehouseErrorCode.STORAGE_LOCATION_NOT_FOUND);
        });
    }

    @Override
    public void checkCodeDuplicate(Long id, String code) {
        storageLocationJpaRepository.findByIdNotAndCode(id, code).ifPresent(storageLocation -> {
            throw new BizException(WarehouseErrorCode.STORAGE_LOCATION_CODE_DUPLICATE);
        });
    }

    @Override
    public void checkExistById(Long locationId) {
        storageLocationJpaRepository.findById(locationId)
                .orElseThrow(() -> new BizException(WarehouseErrorCode.STORAGE_LOCATION_NOT_FOUND));
    }
}
