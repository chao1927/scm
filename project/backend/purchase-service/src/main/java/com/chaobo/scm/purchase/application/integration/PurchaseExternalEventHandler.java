package com.chaobo.scm.purchase.application.integration;

/**
 * 采购外部业务事件处理端口。
 *
 * <p>所有自动消息入口和受控人工补偿入口都必须调用该端口，使事件统一经过
 * Inbox 抢占、应用分发、成功确认和失败记录，禁止接口层绕过幂等边界。
 */
@FunctionalInterface
public interface PurchaseExternalEventHandler {

    /**
     * 幂等消费一个已经发生的外部业务事实。
     *
     * @param event 标准信封还原后的采购外部事件
     */
    void consume(PurchaseExternalEvent event);
}
