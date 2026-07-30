package com.chaobo.scm.supplier.infrastructure.mq;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 验证供应商 RocketMQ V1 事件信封的身份与版本契约。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class SupplierBusinessEventEnvelopeCodecTest {

    private final SupplierBusinessEventEnvelopeCodec codec =
            new SupplierBusinessEventEnvelopeCodec(new ObjectMapper());

    @Test
    void decodesV1Envelope() {
        var envelope = codec.decode(bytes("""
                {"schemaVersion":1,"sourceSystem":"PURCHASE","eventCode":"EVT-1",
                 "eventType":"PurchaseOrderReleased","data":{"purchaseOrderId":1001}}
                """));

        assertThat(envelope.sourceSystem()).isEqualTo("PURCHASE");
        assertThat(envelope.eventCode()).isEqualTo("EVT-1");
        assertThat(envelope.eventType()).isEqualTo("PurchaseOrderReleased");
        assertThat(envelope.data().get("purchaseOrderId").longValue()).isEqualTo(1001L);
    }

    @Test
    void acceptsDocumentedEventVersionAndEventIdAliases() {
        var envelope = codec.decode(bytes("""
                {"eventVersion":"1.0","sourceSystem":"WMS","eventId":"EVT-2",
                 "eventType":"WmsReceiptCompleted","payload":{"asnId":2001}}
                """));

        assertThat(envelope.eventCode()).isEqualTo("EVT-2");
        assertThat(envelope.data().get("asnId").longValue()).isEqualTo(2001L);
    }

    @Test
    void rejectsUnknownVersionAndMissingData() {
        assertThatThrownBy(() -> codec.decode(bytes("""
                {"schemaVersion":2,"sourceSystem":"PURCHASE","eventCode":"EVT-3",
                 "eventType":"PurchaseOrderReleased","data":{}}
                """))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> codec.decode(bytes("""
                {"schemaVersion":1,"sourceSystem":"PURCHASE","eventCode":"EVT-4",
                 "eventType":"PurchaseOrderReleased"}
                """))).isInstanceOf(IllegalArgumentException.class);
    }

    private byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
