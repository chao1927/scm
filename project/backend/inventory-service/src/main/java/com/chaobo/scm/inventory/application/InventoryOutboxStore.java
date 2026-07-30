package com.chaobo.scm.inventory.application;

import java.util.List;

/**
 * 库存 Outbox 投递存储端口。
 *
 * @author SCM Team
 */
public interface InventoryOutboxStore {

    /**
     * 查询仍可重试的待投递或失败事件。
     *
     * @param limit 批次数量
     * @param maxRetries 最大重试次数
     * @return 待投递事件
     */
    List<OutboxEvent> pending(int limit, int maxRetries);

    /**
     * 查询指定失败事件。
     *
     * @param eventCode 事件编码
     * @return 失败 Outbox 事件；不存在时返回 {@code null}
     */
    OutboxEvent findFailed(String eventCode);

    /**
     * 标记消息代理已确认接收事件。
     *
     * @param eventId 事件主键
     */
    void markPublished(long eventId);

    /**
     * 标记投递失败并记录原因。
     *
     * @param eventId 事件主键
     * @param reason 失败原因
     */
    void markFailed(long eventId, String reason);

    /**
     * Outbox 投递读模型。
     */
    record OutboxEvent(
            long id,
            String eventCode,
            String eventType,
            String eventVersion,
            String aggregateType,
            String aggregateId,
            String payloadJson,
            int status,
            int retryCount) {
    }
}
