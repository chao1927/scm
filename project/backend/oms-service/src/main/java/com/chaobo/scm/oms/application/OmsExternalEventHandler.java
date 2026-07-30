package com.chaobo.scm.oms.application;

/**
 * OMS 外部业务事件统一处理端口。
 */
@FunctionalInterface
public interface OmsExternalEventHandler {

    /**
     * 通过现有 Inbox 幂等边界消费事件。
     *
     * @param event 外部业务事实
     */
    void consume(OmsExternalEvent event);
}
