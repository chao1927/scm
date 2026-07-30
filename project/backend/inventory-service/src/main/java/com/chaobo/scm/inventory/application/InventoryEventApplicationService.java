package com.chaobo.scm.inventory.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 库存事件兼容门面。
 *
 * <p>新生产链路统一写 Outbox 后由 RocketMQ 投递；新消费链路统一进入版本化 Inbox 服务。该门面保留原
 * HTTP 运维入口和调拨适配器调用方式，但不再包含字符串拆分或“只改状态不发消息”的伪投递逻辑。
 *
 * @author SCM Team
 */
@Service
public class InventoryEventApplicationService {

    private final InventoryEventPublisher outbox;
    private final InventoryInboundEventApplicationService inbound;
    private final InventoryEventEnvelopeCodec codec;
    private final ObjectProvider<InventoryOutboxDispatchApplicationService> dispatcher;

    public InventoryEventApplicationService(
            InventoryEventPublisher outbox,
            InventoryInboundEventApplicationService inbound,
            InventoryEventEnvelopeCodec codec,
            ObjectProvider<InventoryOutboxDispatchApplicationService> dispatcher) {
        this.outbox = outbox;
        this.inbound = inbound;
        this.codec = codec;
        this.dispatcher = dispatcher;
    }

    /**
     * 在当前业务事务中追加 Outbox。
     *
     * @param type 事件类型
     * @param aggregateType 聚合类型
     * @param aggregateId 聚合标识
     * @param payload 业务载荷 JSON
     */
    public void publish(
            String type,
            String aggregateType,
            String aggregateId,
            String payload) {
        outbox.publish(type, aggregateType, aggregateId, payload);
    }

    /**
     * 手工触发一次真实 RocketMQ 投递扫描。
     *
     * @param limit 扫描上限
     * @return 投递统计
     */
    public DispatchResult dispatch(int limit) {
        InventoryOutboxDispatchApplicationService service = dispatcher.getIfAvailable();
        if (service == null) {
            throw new BusinessException(
                    ErrorCode.STATE_CONFLICT,
                    "测试环境未启用 RocketMQ 投递器");
        }
        InventoryOutboxDispatchApplicationService.DispatchResult result =
                service.dispatch(limit, 16);
        return new DispatchResult(result.published(), result.failed());
    }

    /**
     * 兼容内部 HTTP 入口，将旧请求立即提升为版本化标准信封后走统一 Inbox。
     *
     * @param envelope 兼容事件请求
     * @return 消费结果
     */
    public ConsumeResult consumeWmsEvent(EventEnvelope envelope) {
        Map<String, Object> payload = codec.decodePayload(envelope.payload());
        InventoryEventEnvelope event = new InventoryEventEnvelope(
                envelope.eventCode(),
                envelope.eventType(),
                envelope.eventVersion(),
                envelope.sourceSystem(),
                envelope.sourceSystem(),
                envelope.aggregateType(),
                envelope.aggregateId(),
                envelope.aggregateVersion(),
                envelope.eventCode(),
                envelope.sourceSystem() + ":" + envelope.eventCode(),
                java.time.OffsetDateTime.now().toString(),
                null,
                payload);
        InventoryInboundEventApplicationService.ConsumeResult result =
                inbound.consume(event, codec.encode(event));
        return new ConsumeResult(result.duplicated(), result.message());
    }

    /**
     * 兼容事件请求。
     */
    public record EventEnvelope(
            String sourceSystem,
            String eventCode,
            String eventType,
            String eventVersion,
            String aggregateType,
            String aggregateId,
            long aggregateVersion,
            String payload) {

        public EventEnvelope(
                String sourceSystem,
                String eventCode,
                String eventType,
                String payload) {
            this(
                    sourceSystem,
                    eventCode,
                    eventType,
                    InventoryEventEnvelope.CURRENT_VERSION,
                    eventType,
                    eventCode,
                    1L,
                    payload);
        }
    }

    /**
     * 兼容消费结果。
     */
    public record ConsumeResult(boolean duplicated, String message) {
    }

    /**
     * Outbox 投递统计。
     */
    public record DispatchResult(int published, int failed) {
    }
}
