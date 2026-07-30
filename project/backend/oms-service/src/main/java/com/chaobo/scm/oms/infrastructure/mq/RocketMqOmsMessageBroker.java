package com.chaobo.scm.oms.infrastructure.mq;

import com.chaobo.scm.oms.application.OmsMessageBroker;
import jakarta.annotation.PreDestroy;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * OMS 真实 RocketMQ 生产者。
 *
 * <p>发送前由 {@link OmsEventEnvelopeCodec} 统一编码为 V1 信封；连接或发送失败
 * 直接抛给 Outbox 重试流程，不提供内存、日志或 Noop 降级。
 */
@Component
@ConditionalOnProperty(name = "scm.rocketmq.enabled", havingValue = "true")
public class RocketMqOmsMessageBroker implements OmsMessageBroker {

    private final ClientServiceProvider provider = ClientServiceProvider.loadService();
    private final Producer producer;
    private final OmsEventEnvelopeCodec codec;
    private final String topic;

    public RocketMqOmsMessageBroker(
            OmsEventEnvelopeCodec codec,
            @Value("${scm.rocketmq.endpoints}") String endpoints,
            @Value("${scm.rocketmq.oms-topic:oms-domain-event}") String topic)
            throws ClientException {
        this.codec = codec;
        this.topic = topic;
        ClientConfiguration configuration = ClientConfiguration.newBuilder()
                .setEndpoints(endpoints)
                .build();
        this.producer = provider.newProducerBuilder()
                .setClientConfiguration(configuration)
                .setTopics(topic)
                .build();
    }

    @Override
    public void publish(OutboundMessage message) {
        try {
            producer.send(provider.newMessageBuilder()
                    .setTopic(topic)
                    .setKeys(message.eventCode())
                    .setTag(message.eventType())
                    .setBody(codec.encode(message))
                    .build());
        } catch (ClientException exception) {
            throw new IllegalStateException("OMS 事件 RocketMQ 投递失败", exception);
        }
    }

    /**
     * 释放 RocketMQ 生产者连接。
     *
     * @throws Exception 客户端关闭异常
     */
    @PreDestroy
    public void close() throws Exception {
        producer.close();
    }
}
