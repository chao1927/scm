package com.chaobo.scm.wms.application.shared;

public interface WmsEventPublisher {
    void publish(String eventType, String aggregateType, String aggregateId, int version, String payload);
}
