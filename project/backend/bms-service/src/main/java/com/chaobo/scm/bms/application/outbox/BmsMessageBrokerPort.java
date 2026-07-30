package com.chaobo.scm.bms.application.outbox;

/**
 * BMS 领域事件消息代理端口。
 *
 * @author SCM Team
 */
public interface BmsMessageBrokerPort {

    /**
     * 将领域事件投递到真实消息代理。
     *
     * @param message 待投递消息
     */
    void publish(OutboundMessage message);

    record OutboundMessage(String eventCode, String eventType,
                           String aggregateNo, String businessNo, String payload) {
    }
}
