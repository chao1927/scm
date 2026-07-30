package com.chaobo.scm.purchase.infrastructure.mq;

import com.chaobo.scm.purchase.application.outbox.OutboxMessage;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 采购事件标准信封编解码契约测试。
 */
class PurchaseEventEnvelopeCodecTest {

    private final ObjectMapper json = new ObjectMapper();
    private final PurchaseEventEnvelopeCodec codec = new PurchaseEventEnvelopeCodec(json);

    @Test
    void shouldEncodeOutboxPayloadAsStandardEnvelope() throws Exception {
        var encoded = codec.encode(new OutboxMessage(
                1L,
                "EVT-001",
                "PurchaseOrderCreated",
                "PurchaseOrder",
                "PO-001",
                "{\"orderNo\":\"PO-001\"}",
                0
        ));

        var root = json.readTree(encoded);
        assertEquals(1, root.get("schemaVersion").intValue());
        assertEquals("PURCHASE", root.get("sourceSystem").asText());
        assertEquals("EVT-001", root.get("eventCode").asText());
        assertEquals("PurchaseOrderCreated", root.get("eventType").asText());
        assertEquals("PO-001", root.get("data").get("orderNo").asText());
    }

    @Test
    void shouldUseEnvelopeIdentityInsteadOfUntrustedDataIdentity() {
        var event = codec.decode("""
                {
                  "schemaVersion": 1,
                  "sourceSystem": "WMS",
                  "eventCode": "EVT-002",
                  "eventType": "WmsReceiptCompleted",
                  "data": {
                    "sourceSystem": "FORGED",
                    "eventCode": "FORGED-CODE",
                    "eventType": "FORGED-TYPE",
                    "inboundNo": "IN-001"
                  }
                }
                """.getBytes());

        assertEquals("WMS", event.sourceSystem());
        assertEquals("EVT-002", event.eventCode());
        assertEquals("WmsReceiptCompleted", event.eventType());
        assertEquals("IN-001", event.inboundNo());
    }

    @Test
    void shouldFailClosedForUnknownEnvelopeVersion() {
        assertThrows(IllegalArgumentException.class, () -> codec.decode("""
                {
                  "schemaVersion": 2,
                  "sourceSystem": "WMS",
                  "eventCode": "EVT-003",
                  "eventType": "WmsReceiptCompleted",
                  "data": {}
                }
                """.getBytes()));
    }
}
