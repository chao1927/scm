package org.scm.bdp.service.domain.repository;

import org.scm.bdp.service.domain.model.LogisticsChannelAgg;
import org.scm.common.BaseRepository;

public interface LogisticsChannelRepository extends BaseRepository<LogisticsChannelAgg> {
    void checkExistById(Long id);

}
