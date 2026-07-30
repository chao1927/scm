package com.chaobo.scm.tms.application.outbox;

/**
 * TMS 领域事件消息代理端口。
 *
 * @author SCM Team
 */
public interface TmsMessageBrokerPort {

    /**
     * 把已持久化事件投递给真实消息代理。
     *
     * @param message 待投递的标准业务消息
     */
    void publish(OutboundMessage message);

    /**
     * 待发送的标准业务消息。
     */
    record OutboundMessage(String eventCode, String eventType, String businessNo,
                           String payload) {
    }
}
