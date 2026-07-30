package com.chaobo.scm.inventory.application;

/**
 * 入站库存事件业务处理端口。
 *
 * <p>可靠消费服务只负责 Inbox、版本和顺序；具体事件如何转换成库存命令由该端口实现。
 *
 * @author SCM Team
 */
public interface InventoryInboundEventProcessor {

    /**
     * 把已通过信封校验的外部事实转换为库存业务动作。
     *
     * @param event 标准事件信封
     */
    void process(InventoryEventEnvelope event);
}
