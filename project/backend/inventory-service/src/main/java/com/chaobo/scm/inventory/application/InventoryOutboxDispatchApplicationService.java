package com.chaobo.scm.inventory.application;

import com.chaobo.scm.common.logging.ScmLogContext;
import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 库存 Outbox 真实消息投递应用服务。
 *
 * <p>服务先把历史业务载荷包装为当前标准信封，再同步发送消息；只有 RocketMQ 返回成功才更新 Outbox。
 * 生产环境没有内存或 Noop 代理，连接或发送失败会保留失败状态等待 Broker 重试或人工重放。
 *
 * @author SCM Team
 */
@Service
@Profile("!test")
public class InventoryOutboxDispatchApplicationService implements InventoryOutboundEventReplayer {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryOutboxDispatchApplicationService.class);

    private static final int DEFAULT_BATCH_SIZE = 50;
    private static final int MAX_BATCH_SIZE = 200;
    private static final int DEFAULT_MAX_RETRIES = 16;
    private static final int MAX_ERROR_LENGTH = 1000;

    private final InventoryOutboxStore store;
    private final InventoryMessageBroker broker;
    private final InventoryEventEnvelopeCodec codec;

    public InventoryOutboxDispatchApplicationService(
            InventoryOutboxStore store,
            InventoryMessageBroker broker,
            InventoryEventEnvelopeCodec codec) {
        this.store = store;
        this.broker = broker;
        this.codec = codec;
    }

    /**
     * 投递一批 Outbox 事件。
     *
     * @param requestedLimit 请求批量
     * @param requestedMaxRetries 最大重试次数
     * @return 成功及失败数量
     */
    public DispatchResult dispatch(
            int requestedLimit,
            int requestedMaxRetries) {
        int limit = requestedLimit <= 0
                ? DEFAULT_BATCH_SIZE
                : Math.min(requestedLimit, MAX_BATCH_SIZE);
        int maxRetries = requestedMaxRetries <= 0
                ? DEFAULT_MAX_RETRIES
                : requestedMaxRetries;
        int published = 0;
        int failed = 0;
        for (InventoryOutboxStore.OutboxEvent event : store.pending(limit, maxRetries)) {
            try {
                publish(event);
                published++;
            } catch (RuntimeException exception) {
                failed++;
            }
        }
        return new DispatchResult(published, failed);
    }

    /**
     * 立即重新投递一个已失败 Outbox 事件。
     *
     * @param eventCode 事件编码
     */
    @Override
    public void replay(String eventCode) {
        InventoryOutboxStore.OutboxEvent event = store.findFailed(eventCode);
        if (event == null) {
            throw new com.chaobo.scm.common.error.BusinessException(
                    com.chaobo.scm.common.error.ErrorCode.NOT_FOUND,
                    "出站失败事件不存在或已处理");
        }
        publish(event);
    }

    private void publish(InventoryOutboxStore.OutboxEvent event) {
        try (ScmLogContext ignored = ScmLogContext.openSystem(event.eventCode())) {
            broker.publish(message(event));
            store.markPublished(event.id());
            LOG.info("event=rocketmq_publish operation=inventory_outbox_publish result=SUCCESS eventId={} eventCode={} eventType={}",
                    event.id(), event.eventCode(), event.eventType());
        } catch (RuntimeException exception) {
            store.markFailed(event.id(), errorMessage(exception));
            try (ScmLogContext ignored = ScmLogContext.openSystem(event.eventCode())) {
                LOG.error("event=rocketmq_publish operation=inventory_outbox_publish result=FAILURE eventId={} eventCode={} eventType={}",
                        event.id(), event.eventCode(), event.eventType(), exception);
            }
            throw exception;
        }
    }

    private InventoryOutboxMessage message(InventoryOutboxStore.OutboxEvent event) {
        Map<String, Object> payload = codec.decodePayload(event.payloadJson());
        long aggregateVersion = aggregateVersion(payload);
        InventoryEventEnvelope envelope = new InventoryEventEnvelope(
                event.eventCode(),
                event.eventType(),
                event.eventVersion(),
                "INVENTORY",
                "INVENTORY",
                event.aggregateType(),
                event.aggregateId(),
                aggregateVersion,
                event.aggregateId(),
                event.eventCode(),
                OffsetDateTime.now().toString(),
                null,
                payload);
        return new InventoryOutboxMessage(
                event.eventCode(),
                event.eventType(),
                codec.encode(envelope));
    }

    private static long aggregateVersion(Map<String, Object> payload) {
        Object value = payload.get("version");
        if (value instanceof Number number) {
            return Math.max(1L, number.longValue());
        }
        if (value != null) {
            try {
                return Math.max(1L, Long.parseLong(value.toString()));
            } catch (NumberFormatException ignored) {
                // 旧 Outbox 没有结构化聚合版本时使用首版本，不能生成非法零版本信封。
            }
        }
        return 1L;
    }

    private static String errorMessage(RuntimeException exception) {
        String value = exception.getMessage();
        if (value == null || value.isBlank()) {
            value = exception.getClass().getSimpleName();
        }
        return value.length() <= MAX_ERROR_LENGTH
                ? value
                : value.substring(0, MAX_ERROR_LENGTH);
    }

    /**
     * 单批投递统计。
     */
    public record DispatchResult(int published, int failed) {
    }
}
