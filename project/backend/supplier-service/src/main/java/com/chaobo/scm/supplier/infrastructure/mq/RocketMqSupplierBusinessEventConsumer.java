package com.chaobo.scm.supplier.infrastructure.mq;

import jakarta.annotation.PreDestroy;
import org.apache.rocketmq.client.apis.*;
import org.apache.rocketmq.client.apis.consumer.*;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 供应商协同统一 RocketMQ 业务事件 PushConsumer。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Component
@ConditionalOnProperty(name = "scm.rocketmq.business-consumer.enabled", havingValue = "true")
public class RocketMqSupplierBusinessEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(RocketMqSupplierBusinessEventConsumer.class);
    private final PushConsumer consumer;
    private final SupplierBusinessEventEnvelopeCodec codec;
    private final SupplierBusinessEventDispatcher dispatcher;

    public RocketMqSupplierBusinessEventConsumer(SupplierBusinessEventEnvelopeCodec codec,
            SupplierBusinessEventDispatcher dispatcher,
            @Value("${scm.rocketmq.endpoints}") String endpoints,
            @Value("${scm.rocketmq.business-consumer.topics}") String topics,
            @Value("${scm.rocketmq.business-consumer.group:supplier-business-event-v1}") String group,
            @Value("${scm.rocketmq.business-consumer.threads:4}") int threads) throws Exception {
        this.codec = codec;
        this.dispatcher = dispatcher;
        consumer = ClientServiceProvider.loadService().newPushConsumerBuilder()
                .setClientConfiguration(com.chaobo.scm.common.mq.RocketMqClientConfigurations.create(endpoints))
                .setConsumerGroup(group)
                .setSubscriptionExpressions(subscriptions(topics))
                .setConsumptionThreadCount(threads)
                .setMessageListener(this::consume)
                .build();
    }

    private ConsumeResult consume(MessageView message) {
        try {
            ByteBuffer body = message.getBody().asReadOnlyBuffer();
            byte[] bytes = new byte[body.remaining()];
            body.get(bytes);
            dispatcher.dispatch(codec.decode(bytes));
            return ConsumeResult.SUCCESS;
        } catch (Exception exception) {
            LOG.warn("供应商 RocketMQ 业务事件消费失败，等待重试，messageId={},topic={}",
                    message.getMessageId(), message.getTopic(), exception);
            return ConsumeResult.FAILURE;
        }
    }

    private Map<String, FilterExpression> subscriptions(String topics) {
        if (topics == null || topics.isBlank()) {
            throw new IllegalArgumentException("供应商 RocketMQ 业务 Topic 不能为空");
        }
        return Arrays.stream(topics.split(",")).map(String::trim).filter(value -> !value.isBlank())
                .distinct().collect(Collectors.toUnmodifiableMap(
                        Function.identity(), value -> FilterExpression.SUB_ALL));
    }

    @PreDestroy
    public void close() throws Exception {
        consumer.close();
    }
}
