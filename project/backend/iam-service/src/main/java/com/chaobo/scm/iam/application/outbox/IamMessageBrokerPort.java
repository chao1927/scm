package com.chaobo.scm.iam.application.outbox;

/**
 * IAM 领域事件消息代理端口。
 *
 * @author SCM Team
 */
public interface IamMessageBrokerPort {

    /**
     * 将事务 Outbox 事件投递到真实消息代理。
     *
     * @param message 待投递消息
     */
    void publish(OutboundMessage message);

    /** IAM 出站消息快照。 */
    record OutboundMessage(String eventCode, String eventType,
                           String businessNo, String payload) {
    }
}
