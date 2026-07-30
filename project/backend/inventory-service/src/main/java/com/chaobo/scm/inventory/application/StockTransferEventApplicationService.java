package com.chaobo.scm.inventory.application;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * 调拨事件兼容门面。
 *
 * <p>原内部 HTTP 请求会转换为 V1 标准信封并进入统一 Inbox；RocketMQ Listener 直接使用相同应用服务，
 * 因而重复、乱序、失败和重放遵循同一套规则。
 *
 * @author SCM Team
 */
@Service
public class StockTransferEventApplicationService {

    private final InventoryInboundEventApplicationService inbound;
    private final InventoryEventEnvelopeCodec codec;

    public StockTransferEventApplicationService(
            InventoryInboundEventApplicationService inbound,
            InventoryEventEnvelopeCodec codec) {
        this.inbound = inbound;
        this.codec = codec;
    }

    /**
     * 消费调拨事实。
     *
     * @param event 调拨事件
     * @return 幂等消费结果
     */
    public ConsumeResult consume(EventEnvelope event) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("transferNo", event.transferNo());
        payload.put("qty", event.qty());
        payload.put("finalReceipt", event.finalReceipt());
        InventoryEventEnvelope envelope = new InventoryEventEnvelope(
                event.eventCode(),
                event.eventType(),
                event.eventVersion(),
                event.sourceSystem(),
                event.sourceSystem(),
                "StockTransfer",
                event.transferNo(),
                event.aggregateVersion(),
                event.transferNo(),
                event.sourceSystem() + ":" + event.eventCode(),
                java.time.OffsetDateTime.now().toString(),
                null,
                payload);
        InventoryInboundEventApplicationService.ConsumeResult result =
                inbound.consume(envelope, codec.encode(envelope));
        return new ConsumeResult(result.duplicated(), result.message());
    }

    /**
     * 调拨兼容事件信封。
     */
    public record EventEnvelope(
            String sourceSystem,
            String eventCode,
            String eventType,
            String transferNo,
            BigDecimal qty,
            boolean finalReceipt,
            String eventVersion,
            long aggregateVersion) {

        public EventEnvelope(
                String sourceSystem,
                String eventCode,
                String eventType,
                String transferNo,
                BigDecimal qty,
                boolean finalReceipt,
                int aggregateVersion) {
            this(
                    sourceSystem,
                    eventCode,
                    eventType,
                    transferNo,
                    qty,
                    finalReceipt,
                    InventoryEventEnvelope.CURRENT_VERSION,
                    aggregateVersion);
        }
    }

    /**
     * 调拨消费结果。
     */
    public record ConsumeResult(boolean duplicated, String message) {
    }
}
