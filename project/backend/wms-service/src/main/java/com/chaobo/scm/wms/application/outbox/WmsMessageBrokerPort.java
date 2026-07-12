package com.chaobo.scm.wms.application.outbox;

public interface WmsMessageBrokerPort {
    void publish(String eventCode, String eventType, String payload);
}
