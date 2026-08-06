package com.chaobo.scm.mdm.application.outbox;

/**
 * 主数据领域事件的真实消息代理边界。
 *
 * @author SCM Team
 */
public interface MdmMessageBrokerPort {

    /**
     * 将事务 Outbox 事件发送到指定主题。
     *
     * @param message 待发送消息
     */
    void publish(OutboundMessage message);

    /** 主数据出站消息快照。 */
    record OutboundMessage(String eventCode, String eventType, String businessNo,
                           String payload, String destinationTopic) {
    }
}
