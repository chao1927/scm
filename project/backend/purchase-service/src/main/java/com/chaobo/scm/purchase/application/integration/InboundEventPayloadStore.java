package com.chaobo.scm.purchase.application.integration;

import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class InboundEventPayloadStore {
    private final InboundEventLogPort logs;
    private final ObjectMapper json;

    public InboundEventPayloadStore(InboundEventLogPort logs, ObjectMapper json) {
        this.logs = logs;
        this.json = json;
    }

    public void save(String sourceSystem, String eventCode, String consumerName, Object payload) {
        try {
            logs.savePayload(sourceSystem, eventCode, consumerName, json.writeValueAsString(payload));
        } catch (Exception exception) {
            throw new IllegalStateException("入站事件原始载荷保存失败", exception);
        }
    }
}
