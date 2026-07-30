package com.chaobo.scm.oms.application;

/**
 * OMS 领域事件发布端口。
 *
 * <p>应用层只依赖该端口确认消息是否已被消息代理接收，基础设施层必须提供真实
 * RocketMQ 实现。生产环境不允许使用内存、日志或 Noop 实现。
 */
public interface OmsMessageBroker {

    /**
     * 发布标准 V1 信封事件。
     *
     * @param message 待发布的 Outbox 消息
     */
    void publish(OutboundMessage message);

    /**
     * Outbox 向消息代理传递的不可变消息。
     *
     * @param eventCode 全局幂等事件编码
     * @param eventType 事件类型
     * @param businessNo 业务单号
     * @param payload 业务载荷
     */
    record OutboundMessage(String eventCode, String eventType,
                           String businessNo, String payload) {
    }
}
