package com.chaobo.scm.inventory.application;

import java.util.List;

/**
 * 库存事件失败查询与人工重放审计存储端口。
 *
 * <p>查询只暴露失败记录；重放注册由数据库幂等键唯一约束，保证重复人工请求不会再次执行库存动作。
 *
 * @author SCM Team
 */
public interface InventoryEventFailureStore {

    /**
     * 分页查询失败事件。
     *
     * @param direction 事件方向
     * @param offset 数据偏移量
     * @param limit 返回数量
     * @return 失败事件分页
     */
    FailurePage failures(Direction direction, int offset, int limit);

    /**
     * 查询可重放的失败事件。
     *
     * @param direction 事件方向
     * @param eventCode 事件编码
     * @return 失败事件；不存在或已成功时返回 {@code null}
     */
    FailureEvent findFailure(Direction direction, String eventCode);

    /**
     * 注册人工重放审计记录。
     *
     * @param idempotencyKey 请求幂等键
     * @param direction 事件方向
     * @param eventCode 事件编码
     * @param reason 重放原因
     * @param operatorId 操作人 ID
     * @return 注册结果
     */
    ReplayRegistration registerReplay(
            String idempotencyKey,
            Direction direction,
            String eventCode,
            String reason,
            long operatorId);

    /**
     * 标记人工重放成功。
     *
     * @param replayId 重放审计 ID
     */
    void markReplaySucceeded(long replayId);

    /**
     * 标记人工重放失败。
     *
     * @param replayId 重放审计 ID
     * @param reason 失败原因
     */
    void markReplayFailed(long replayId, String reason);

    /**
     * 事件方向。
     */
    enum Direction {
        /** 外部事件进入库存上下文。 */
        INBOUND,
        /** 库存领域事件投递到外部上下文。 */
        OUTBOUND
    }

    /**
     * 失败事件读模型。
     */
    record FailureEvent(
            Direction direction,
            String eventCode,
            String eventType,
            String eventVersion,
            String aggregateType,
            String aggregateId,
            int status,
            int retryCount,
            String lastError,
            String rawJson) {
    }

    /**
     * 失败事件分页。
     */
    record FailurePage(long total, List<FailureEvent> records) {
    }

    /**
     * 重放注册结果。
     *
     * @param replayId 审计 ID
     * @param newlyRegistered 是否本次首次注册
     * @param replayStatus 已有或新增记录状态
     */
    record ReplayRegistration(
            long replayId,
            boolean newlyRegistered,
            int replayStatus) {
    }
}
