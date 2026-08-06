package com.chaobo.scm.mdm.infrastructure.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RocketMqMdmInboundEventConsumerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void mapsStandardEnvelopeToInboxCommand() throws Exception {
        var event = RocketMqMdmInboundEventConsumer.toEvent(objectMapper.readTree("""
            {"schemaVersion":1,"sourceSystem":"SUP","eventCode":"evt-1",
             "eventType":"SupplierProfileChangeSubmitted","businessNo":"SUP-1",
             "data":{"typeCode":"SUPPLIER","dataCode":"SUP-1","idempotencyKey":"idem-1"}}
            """));

        assertThat(event.eventId()).isEqualTo("evt-1");
        assertThat(event.businessKey()).isEqualTo("SUP-1");
        assertThat(event.typeCode()).isEqualTo("SUPPLIER");
        assertThat(event.idempotencyKey()).isEqualTo("idem-1");
    }

    @Test
    void rejectsUnknownSchemaForBrokerRetry() throws Exception {
        var root = objectMapper.readTree("""
            {"schemaVersion":2,"sourceSystem":"SUP","eventCode":"evt-1",
             "eventType":"SupplierChanged","businessNo":"SUP-1","data":{}}
            """);

        assertThatThrownBy(() -> RocketMqMdmInboundEventConsumer.toEvent(root))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("version");
    }
}
