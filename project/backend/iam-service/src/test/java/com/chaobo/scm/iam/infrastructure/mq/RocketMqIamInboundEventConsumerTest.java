package com.chaobo.scm.iam.infrastructure.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RocketMqIamInboundEventConsumerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void mapsVersionedEnvelopeToInboxCommand() throws Exception {
        var event = RocketMqIamInboundEventConsumer.toEvent(objectMapper.readTree("""
            {"schemaVersion":1,"sourceSystem":"MDM","eventCode":"MDM-1",
             "eventType":"MasterDataPublished","businessNo":"SKU-1","data":{"version":2}}
            """));

        assertThat(event.eventId()).isEqualTo("MDM-1");
        assertThat(event.sourceSystem()).isEqualTo("MDM");
        assertThat(event.payload()).contains("\"version\":2");
    }

    @Test
    void rejectsUnknownSchemaVersionForBrokerRetry() throws Exception {
        var root = objectMapper.readTree("""
            {"schemaVersion":2,"sourceSystem":"MDM","eventCode":"MDM-1",
             "eventType":"MasterDataPublished","businessNo":"SKU-1","data":{}}
            """);

        assertThatThrownBy(() -> RocketMqIamInboundEventConsumer.toEvent(root))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("unsupported");
    }
}
