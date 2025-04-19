package org.scm.ecs.domain.repository;

import org.scm.common.BaseRepository;
import org.scm.ecs.domain.model.EcommOrderAgg;

public interface EcommOrderRepository extends BaseRepository<EcommOrderAgg> {
    // 可以添加按平台订单号查询的方法
    EcommOrderAgg findByPlatformOrderNo(String platformOrderNo);
}
