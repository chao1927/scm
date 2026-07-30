package com.chaobo.scm.inventory.application;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 库存事件标准信封编解码契约测试。
 *
 * <p>测试使用嵌套对象和数组证明事件处理不再依赖逗号拆分的扁平 JSON 解析器。
 *
 * @author SCM Team
 */
class InventoryEventEnvelopeCodecTest {

    @Test
    void parsesVersionedEnvelopeAndPreservesNestedPayload() {
        InventoryEventEnvelopeCodec codec =
                new InventoryEventEnvelopeCodec(new ObjectMapper());

        InventoryEventEnvelope event = codec.decode("""
                {
                  "eventId":"WMS-EVT-1",
                  "eventType":"InboundOrderPutawayCompleted",
                  "eventVersion":"1.0",
                  "sourceContext":"WMS",
                  "sourceSystem":"WMS",
                  "aggregateType":"InboundOrder",
                  "aggregateId":"IN-1",
                  "aggregateVersion":1,
                  "businessKey":"IN-1",
                  "idempotencyKey":"WMS:IN-1:PUTAWAY:1",
                  "occurredAt":"2026-07-30T10:00:00+08:00",
                  "traceId":"TRACE-1",
                  "payload":{
                    "ownerId":88,
                    "warehouseId":99,
                    "sku":"SKU-1",
                    "putawayQty":3.5000,
                    "sourceNo":"IN-1",
                    "attributes":{"qualityStatus":"QUALIFIED"},
                    "lines":[{"lineNo":"1"}]
                  }
                }
                """);

        assertThat(event.eventId()).isEqualTo("WMS-EVT-1");
        assertThat(event.eventVersion()).isEqualTo("1.0");
        assertThat(event.aggregateVersion()).isEqualTo(1L);
        assertThat(event.requiredDecimal("putawayQty"))
                .isEqualByComparingTo(new BigDecimal("3.5"));
        assertThat(event.payload()).containsKeys("attributes", "lines");
        assertThat(codec.decode(codec.encode(event))).isEqualTo(event);
    }
}
