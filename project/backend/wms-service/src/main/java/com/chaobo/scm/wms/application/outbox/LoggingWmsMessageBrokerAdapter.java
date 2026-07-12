package com.chaobo.scm.wms.application.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingWmsMessageBrokerAdapter implements WmsMessageBrokerPort {
    private static final Logger log = LoggerFactory.getLogger(LoggingWmsMessageBrokerAdapter.class);

    @Override
    public void publish(String eventCode, String eventType, String payload) {
        log.info("WMS outbox event ready: code={}, type={}, payload={}", eventCode, eventType, payload);
    }
}
