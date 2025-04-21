package org.scm.bdp.service.adapter.repository.impl;

import org.scm.bdp.service._share.enums.errorcode.UnitOfMeasureErrorCode;
import org.scm.bdp.service.adapter.infra.jpa.UnitOfMeasureJpaRepository;
import org.scm.bdp.service.domain.model.UnitOfMeasureAgg;
import org.scm.bdp.service.domain.repository.UnitOfMeasureRepository;
import org.scm.common.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UnitOfMeasureRepositoryImpl implements UnitOfMeasureRepository {

    @Autowired
    private UnitOfMeasureJpaRepository unitOfMeasureJpaRepository;

    @Override
    public void save(UnitOfMeasureAgg unitOfMeasureAgg) {
        unitOfMeasureJpaRepository.save(unitOfMeasureAgg.unitOfMeasure());
    }

    @Override
    public UnitOfMeasureAgg findById(Long id) {
        return unitOfMeasureJpaRepository.findById(id)
                .map(UnitOfMeasureAgg::new)
                .orElseThrow(() -> new BizException(UnitOfMeasureErrorCode.UNIT_OF_MEASURE_NOT_FOUND));
    }

    @Override
    public void deleteById(Long id) {
        unitOfMeasureJpaRepository.deleteById(id);
    }

    @Override
    public void checkExistById(Long unitId) {
        unitOfMeasureJpaRepository.findById(unitId)
                .orElseThrow(() -> {
                    throw new BizException(UnitOfMeasureErrorCode.UNIT_OF_MEASURE_EXIST);
                });
    }
}
