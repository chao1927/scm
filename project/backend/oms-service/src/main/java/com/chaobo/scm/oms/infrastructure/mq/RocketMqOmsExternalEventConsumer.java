package com.chaobo.scm.oms.infrastructure.mq;

import com.chaobo.scm.oms.application.OmsExternalEventHandler;
import jakarta.annotation.PreDestroy;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.consumer.FilterExpression;
import org.apache.rocketmq.client.apis.consumer.PushConsumer;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * OMS 跨上下文业务事件的真实 RocketMQ 消费者。
 *
 * <p>消息必须符合标准 V1 信封。未知版本、反序列化错误及业务处理失败一律返回
 * {@link ConsumeResult#FAILURE}，由 RocketMQ 重投；成功处理及 Inbox 幂等命中
 * 返回 {@link ConsumeResult#SUCCESS}。
 */
@Component
@ConditionalOnProperty(name = "scm.rocketmq.enabled", havingValue = "true")
public class RocketMqOmsExternalEventConsumer {

    private static final Logger LOG =
            LoggerFactory.getLogger(RocketMqOmsExternalEventConsumer.class);

    private final PushConsumer consumer;
    private final OmsEventEnvelopeCodec codec;
    private final OmsExternalEventHandler handler;

    public RocketMqOmsExternalEventConsumer(
            OmsEventEnvelopeCodec codec,
            OmsExternalEventHandler handler,
            @Value("${scm.rocketmq.endpoints}") String endpoints,
            @Value("${scm.rocketmq.external-consumer.topics:"
                    + "inventory-domain-event,wms-domain-event,"
                    + "tms-domain-event,bms-domain-event}") String topics,
            @Value("${scm.rocketmq.external-consumer.group:"
                    + "oms-business-event-consumer}") String consumerGroup,
            @Value("${scm.rocketmq.external-consumer.threads:4}")
            int threadCount) throws Exception {
        this.codec = codec;
        this.handler = handler;
        Map<String, FilterExpression> subscriptions = subscriptions(topics);
        this.consumer = ClientServiceProvider.loadService()
                .newPushConsumerBuilder()
                .setClientConfiguration(ClientConfiguration.newBuilder()
                        .setEndpoints(endpoints)
                        .build())
                .setConsumerGroup(consumerGroup)
                .setSubscriptionExpressions(subscriptions)
                .setConsumptionThreadCount(Math.max(1, threadCount))
                .setMessageListener(this::consume)
                .build();
    }

    RocketMqOmsExternalEventConsumer(OmsEventEnvelopeCodec codec,
                                     OmsExternalEventHandler handler) {
        this.codec = codec;
        this.handler = handler;
        this.consumer = null;
    }

    ConsumeResult consume(MessageView message) {
        try {
            ByteBuffer body = message.getBody().asReadOnlyBuffer();
            byte[] bytes = new byte[body.remaining()];
            body.get(bytes);
            handler.consume(codec.decode(bytes));
            return ConsumeResult.SUCCESS;
        } catch (Exception exception) {
            LOG.warn("OMS RocketMQ 事件处理失败，等待代理重试，messageId={}",
                    message.getMessageId(), exception);
            return ConsumeResult.FAILURE;
        }
    }

    private static Map<String, FilterExpression> subscriptions(String topics) {
        Map<String, FilterExpression> result = Arrays.stream(topics.split(","))
                .map(String::trim)
                .filter(topic -> !topic.isEmpty())
                .distinct()
                .collect(Collectors.toUnmodifiableMap(
                        topic -> topic, topic -> FilterExpression.SUB_ALL));
        if (result.isEmpty()) {
            throw new IllegalArgumentException(
                    "OMS RocketMQ consumer topics 不能为空");
        }
        return result;
    }

    /**
     * 释放 RocketMQ 消费者连接。
     *
     * @throws Exception 客户端关闭异常
     */
    @PreDestroy
    public void close() throws Exception {
        if (consumer != null) {
            consumer.close();
        }
    }
}
