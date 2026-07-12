package com.chaobo.scm.wms.infrastructure.persistence.event;

import com.chaobo.scm.wms.application.shared.WmsEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.concurrent.atomic.AtomicLong;

@Repository
public class MyBatisWmsEventPublisher implements WmsEventPublisher {
    private final WmsEventMapper mapper;
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    public MyBatisWmsEventPublisher(WmsEventMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void publish(String type, String aggregateType, String aggregateId, int version, String payload) {
        long id = ids.incrementAndGet();
        mapper.insert(id, "WMS-" + type + "-" + id, type, aggregateType, aggregateId, version, payload);
    }
}
