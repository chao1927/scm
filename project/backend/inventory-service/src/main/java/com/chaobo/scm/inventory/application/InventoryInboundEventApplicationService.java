package com.chaobo.scm.inventory.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import org.springframework.stereotype.Service;

/**
 * 版本化入站库存事件可靠消费服务。
 *
 * <p>处理顺序固定为注册 Inbox、校验信封版本和聚合顺序、执行库存动作、推进游标并标记成功。主事务失败后，
 * 失败原因在独立事务保存，因此 Broker 重投和人工重放都能从原始信封恢复且不会重复记账。
 *
 * @author SCM Team
 */
@Service
public class InventoryInboundEventApplicationService {

    public static final String CONSUMER_NAME = "inventory-domain-event";
    private static final long FIRST_AGGREGATE_VERSION = 1L;
    private static final int MAX_ERROR_LENGTH = 1000;

    private final InventoryInboundEventStore store;
    private final InventoryInboundEventProcessor processor;
    private final InventoryEventTransactions transactions;

    public InventoryInboundEventApplicationService(
            InventoryInboundEventStore store,
            InventoryInboundEventProcessor processor,
            InventoryEventTransactions transactions) {
        this.store = store;
        this.processor = processor;
        this.transactions = transactions;
    }

    /**
     * 可靠消费一个标准事件信封。
     *
     * @param event 已解析信封
     * @param envelopeJson 完整原始信封，用于失败查询和重放
     * @return 消费结果
     */
    public ConsumeResult consume(
            InventoryEventEnvelope event,
            String envelopeJson) {
        validateIdentity(event, envelopeJson);
        InventoryInboundEventStore.InboxEvent inbox = transactions.requiresNew(
                () -> register(event, envelopeJson));
        if (completed(inbox)) {
            return new ConsumeResult(true, inbox.status(), "事件已完成");
        }
        try {
            return transactions.required(() -> process(event, inbox.id()));
        } catch (RuntimeException exception) {
            transactions.requiresNew(() -> {
                InventoryInboundEventStore.InboxEvent latest = store.find(
                        event.sourceSystem(), event.eventId(), CONSUMER_NAME);
                if (latest == null || latest.status() != InventoryInboundEventStore.STATUS_WAITING_REPLAY) {
                    store.markFailed(inbox.id(), errorMessage(exception));
                }
                return null;
            });
            throw exception;
        }
    }

    private InventoryInboundEventStore.InboxEvent register(
            InventoryEventEnvelope event,
            String envelopeJson) {
        InventoryInboundEventStore.InboxEvent existing =
                store.find(event.sourceSystem(), event.eventId(), CONSUMER_NAME);
        if (existing != null) {
            return existing;
        }
        return store.register(event, CONSUMER_NAME, envelopeJson);
    }

    private ConsumeResult process(
            InventoryEventEnvelope event,
            long inboxId) {
        InventoryInboundEventStore.InboxEvent current =
                store.find(event.sourceSystem(), event.eventId(), CONSUMER_NAME);
        if (current != null && completed(current)) {
            return new ConsumeResult(true, current.status(), "事件已完成");
        }
        requireSupportedVersion(event);
        InventoryInboundEventStore.EventCursor cursor = store.findCursor(
                event.sourceSystem(),
                event.aggregateType(),
                event.aggregateId(),
                CONSUMER_NAME);
        if (cursor == null && event.aggregateVersion() != FIRST_AGGREGATE_VERSION) {
            throw waitingReplay(inboxId, event.aggregateVersion(), 0L);
        }
        if (cursor != null && event.aggregateVersion() <= cursor.aggregateVersion()) {
            store.markIgnored(inboxId, "聚合版本已处理");
            return new ConsumeResult(false, InventoryInboundEventStore.STATUS_IGNORED, "过期事件已忽略");
        }
        if (cursor != null && event.aggregateVersion() != cursor.aggregateVersion() + 1L) {
            throw waitingReplay(inboxId, event.aggregateVersion(), cursor.aggregateVersion());
        }
        processor.process(event);
        advanceCursor(event, cursor);
        store.markSucceeded(inboxId);
        return new ConsumeResult(false, InventoryInboundEventStore.STATUS_SUCCEEDED, "处理成功");
    }

    private void advanceCursor(
            InventoryEventEnvelope event,
            InventoryInboundEventStore.EventCursor cursor) {
        if (cursor == null) {
            store.createCursor(event, CONSUMER_NAME);
            return;
        }
        if (!store.advanceCursor(event, CONSUMER_NAME, cursor.aggregateVersion())) {
            throw new BusinessException(
                    ErrorCode.VERSION_CONFLICT,
                    "库存事件聚合游标并发冲突");
        }
    }

    private static void validateIdentity(
            InventoryEventEnvelope event,
            String envelopeJson) {
        if (event == null
                || blank(event.eventId())
                || blank(event.eventType())
                || blank(event.sourceSystem())
                || blank(event.aggregateType())
                || blank(event.aggregateId())
                || blank(envelopeJson)
                || event.aggregateVersion() <= 0) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_FAILED,
                    "库存入站事件信封不完整");
        }
    }

    private static void requireSupportedVersion(InventoryEventEnvelope event) {
        if (!InventoryEventEnvelope.CURRENT_VERSION.equals(event.eventVersion())) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_FAILED,
                    "不支持的事件版本: " + event.eventVersion());
        }
    }

    private static BusinessException outOfOrder(long actual, long last) {
        return new BusinessException(
                ErrorCode.STATE_CONFLICT,
                "事件乱序，当前聚合版本=" + last + "，收到版本=" + actual);
    }

    private BusinessException waitingReplay(long inboxId, long actual, long last) {
        BusinessException exception = outOfOrder(actual, last);
        store.markWaitingReplay(inboxId, exception.getMessage());
        return exception;
    }

    private static boolean completed(InventoryInboundEventStore.InboxEvent inbox) {
        return inbox.status() == InventoryInboundEventStore.STATUS_SUCCEEDED
                || inbox.status() == InventoryInboundEventStore.STATUS_IGNORED;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
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
     * 入站事件消费结果。
     */
    public record ConsumeResult(boolean duplicated, int status, String message) {
    }
}
