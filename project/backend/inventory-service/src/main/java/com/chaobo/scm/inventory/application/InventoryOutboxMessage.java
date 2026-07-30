package com.chaobo.scm.inventory.application;

/**
 * 待发送给消息代理的库存标准事件消息。
 *
 * @author SCM Team
 */
public record InventoryOutboxMessage(
        String eventCode,
        String eventType,
        String envelopeJson) {
}
