package com.chaobo.scm.inventory.application;

/**
 * 库存领域事件 Outbox 写入端口。
 *
 * @author SCM Team
 */
public interface InventoryEventPublisher {

    /**
     * 在当前业务事务中写入 Outbox。
     *
     * @param eventType 事件类型
     * @param aggregateType 聚合类型
     * @param aggregateId 聚合业务标识
     * @param payloadJson 业务载荷 JSON
     */
    void publish(
            String eventType,
            String aggregateType,
            String aggregateId,
            String payloadJson);
}
