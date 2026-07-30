package com.chaobo.scm.inventory.application;

/**
 * 入站库存事件 Inbox 与聚合顺序游标端口。
 *
 * <p>Inbox 唯一标识消费一次性，游标保证同一来源聚合按版本连续推进，两者共同防止重复或乱序记账。
 *
 * @author SCM Team
 */
public interface InventoryInboundEventStore {

    int STATUS_PROCESSING = 1;
    int STATUS_SUCCEEDED = 2;
    int STATUS_FAILED = 3;
    int STATUS_IGNORED = 4;

    /**
     * 查找指定消费者的 Inbox 记录。
     *
     * @param sourceSystem 来源系统
     * @param eventId 事件 ID
     * @param consumerName 消费者名称
     * @return Inbox 记录；不存在时返回 {@code null}
     */
    InboxEvent find(String sourceSystem, String eventId, String consumerName);

    /**
     * 注册首次收到的事件；并发重复时应返回唯一键对应的原记录。
     *
     * @param event 标准事件信封
     * @param consumerName 消费者名称
     * @param envelopeJson 完整信封 JSON
     * @return 注册或已存在的 Inbox 记录
     */
    InboxEvent register(
            InventoryEventEnvelope event,
            String consumerName,
            String envelopeJson);

    /**
     * 标记消费成功。
     *
     * @param inboxId Inbox ID
     */
    void markSucceeded(long inboxId);

    /**
     * 标记过期事件已安全忽略。
     *
     * @param inboxId Inbox ID
     * @param reason 忽略原因
     */
    void markIgnored(long inboxId, String reason);

    /**
     * 标记消费失败并累计重试次数。
     *
     * @param inboxId Inbox ID
     * @param reason 失败原因
     */
    void markFailed(long inboxId, String reason);

    /**
     * 查询同一来源聚合的最后成功版本。
     *
     * @param sourceSystem 来源系统
     * @param aggregateType 聚合类型
     * @param aggregateId 聚合 ID
     * @param consumerName 消费者名称
     * @return 顺序游标；不存在时返回 {@code null}
     */
    EventCursor findCursor(
            String sourceSystem,
            String aggregateType,
            String aggregateId,
            String consumerName);

    /**
     * 首次成功消费后创建聚合版本游标。
     *
     * @param event 事件信封
     * @param consumerName 消费者名称
     */
    void createCursor(
            InventoryEventEnvelope event,
            String consumerName);

    /**
     * 按期望版本推进聚合游标。
     *
     * @param event 新事件
     * @param consumerName 消费者名称
     * @param expectedVersion 更新前版本
     * @return 是否成功推进
     */
    boolean advanceCursor(
            InventoryEventEnvelope event,
            String consumerName,
            long expectedVersion);

    /**
     * Inbox 持久化读模型。
     */
    record InboxEvent(
            long id,
            String sourceSystem,
            String eventId,
            String eventType,
            String eventVersion,
            String aggregateType,
            String aggregateId,
            long aggregateVersion,
            String consumerName,
            String envelopeJson,
            int status,
            int retryCount,
            String lastError,
            String ignoredReason) {
    }

    /**
     * 来源聚合最后成功消费版本。
     */
    record EventCursor(long aggregateVersion, String eventId) {
    }
}
