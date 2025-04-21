package org.scm.bdp.service.adapter.repository.impl;

import org.scm.bdp.service.adapter.infra.jpa.LogisticsChannelJpaRepository;
import org.scm.bdp.service.domain.model.LogisticsChannelAgg;
import org.scm.bdp.service.domain.repository.LogisticsChannelRepository;
import org.scm.common.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.scm.bdp.service._share.enums.errorcode.LogisticsChannelErrorCode;

@Component
public class LogisticsChannelRepositoryImpl implements LogisticsChannelRepository {

    @Autowired
    private LogisticsChannelJpaRepository logisticsChannelJpaRepository;

    @Override
    public void save(LogisticsChannelAgg logisticsChannel) {
        logisticsChannelJpaRepository.save(logisticsChannel.logisticsChannel());
    }

    @Override
    public LogisticsChannelAgg findById(Long id) {
        return logisticsChannelJpaRepository.findById(id).map(LogisticsChannelAgg::new).orElseThrow(
                () -> new BizException(LogisticsChannelErrorCode.LOGISTICS_CHANNEL_NOT_FOUND)
        );
    }

    @Override
    public void deleteById(Long id) {
        logisticsChannelJpaRepository.deleteById(id);
    }

    @Override
    public void checkExistById(Long id) {
        logisticsChannelJpaRepository.findById(id).orElseThrow(() -> new BizException(LogisticsChannelErrorCode.LOGISTICS_CHANNEL_NOT_FOUND));
    }
}
