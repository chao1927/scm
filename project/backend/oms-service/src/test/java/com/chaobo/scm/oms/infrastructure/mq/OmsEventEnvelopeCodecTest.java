package com.chaobo.scm.oms.infrastructure.mq;

import com.chaobo.scm.oms.application.OmsMessageBroker;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 标准 V1 事件信封编解码契约测试。
 */
class OmsEventEnvelopeCodecTest {

    private final ObjectMapper json = new ObjectMapper();
    private final OmsEventEnvelopeCodec codec = new OmsEventEnvelopeCodec(json);

    @Test
    void encodesOutboxAsStandardV1Envelope() throws Exception {
        byte[] bytes = codec.encode(new OmsMessageBroker.OutboundMessage(
                "OMS-E1", "SalesOrderCreated", "SO-1", "{\"amount\":20}"));

        JsonNode root = json.readTree(bytes);
        assertThat(root.get("schemaVersion").asInt()).isEqualTo(1);
        assertThat(root.get("sourceSystem").asText()).isEqualTo("OMS");
        assertThat(root.get("eventCode").asText()).isEqualTo("OMS-E1");
        assertThat(root.get("eventType").asText()).isEqualTo("SalesOrderCreated");
        assertThat(root.get("data").get("businessNo").asText()).isEqualTo("SO-1");
        assertThat(root.get("data").get("payload").get("amount").asInt())
                .isEqualTo(20);
    }

    @Test
    void decodesExternalV1EnvelopeAndRejectsUnknownVersion() {
        String valid = """
                {"schemaVersion":1,"sourceSystem":"INVENTORY",
                 "eventCode":"INV-E1","eventType":"StockReserved",
                 "data":{"businessNo":"SO-1","fulfillmentNo":"FUL-1",
                         "reservationRefNo":"REF-1","reservationNo":"INV-1",
                         "quantity":2}}
                """;

        var event = codec.decode(valid.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        assertThat(event.sourceSystem()).isEqualTo("INVENTORY");
        assertThat(event.eventCode()).isEqualTo("INV-E1");
        assertThat(event.quantity()).isEqualByComparingTo("2");

        assertThatThrownBy(() -> codec.decode(
                valid.replace("\"schemaVersion\":1", "\"schemaVersion\":2")
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("版本");
    }
}
