package com.chaobo.scm.inventory.application;

/**
 * 库存业务消息代理端口。
 *
 * <p>生产实现必须连接真实 RocketMQ；内存实现仅允许放在测试源码中。
 *
 * @author SCM Team
 */
public interface InventoryMessageBroker {

    /**
     * 同步发送标准库存事件，只有 Broker 确认成功才允许正常返回。
     *
     * @param message 标准事件消息
     */
    void publish(InventoryOutboxMessage message);
}
