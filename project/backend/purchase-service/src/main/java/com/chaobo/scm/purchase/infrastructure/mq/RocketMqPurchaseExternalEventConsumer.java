package com.chaobo.scm.purchase.infrastructure.mq;

import com.chaobo.scm.purchase.application.integration.PurchaseExternalEventHandler;
import jakarta.annotation.PreDestroy;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.consumer.FilterExpression;
import org.apache.rocketmq.client.apis.consumer.MessageListener;
import org.apache.rocketmq.client.apis.consumer.PushConsumer;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 采购外部业务事件的真实 RocketMQ PushConsumer。
 *
 * <p>该消费者是自动业务事件的唯一主入口。消息解析后进入现有 Inbox 应用服务；
 * 解析、版本、持久化或业务处理任一失败均返回 {@link ConsumeResult#FAILURE}，
 * 由 Broker 执行重投，不允许转入 HTTP 或内存降级通道。
 */
@Component
@ConditionalOnProperty(name = "scm.rocketmq.enabled", havingValue = "true")
public class RocketMqPurchaseExternalEventConsumer implements MessageListener {

    private static final Logger LOG =
            LoggerFactory.getLogger(RocketMqPurchaseExternalEventConsumer.class);

    private final PurchaseEventEnvelopeCodec codec;
    private final PurchaseExternalEventHandler handler;
    private final PushConsumer consumer;

    @Autowired
    public RocketMqPurchaseExternalEventConsumer(
            PurchaseEventEnvelopeCodec codec,
            PurchaseExternalEventHandler handler,
            @Value("${scm.rocketmq.endpoints}") String endpoints,
            @Value("${scm.rocketmq.external-consumer.topics:"
                    + "supplier-domain-event,wms-domain-event,tms-domain-event,bms-domain-event}")
            String topics,
            @Value("${scm.rocketmq.external-consumer.group:"
                    + "purchase-business-event-consumer}") String group,
            @Value("${scm.rocketmq.external-consumer.threads:4}") int threads)
            throws Exception {
        this.codec = codec;
        this.handler = handler;
        Map<String, FilterExpression> subscriptions = subscriptions(topics);
        if (group == null || group.isBlank()) {
            throw new IllegalArgumentException("采购 RocketMQ 消费组不能为空");
        }
        if (threads <= 0) {
            throw new IllegalArgumentException("采购 RocketMQ 消费线程数必须大于零");
        }
        this.consumer = ClientServiceProvider.loadService()
                .newPushConsumerBuilder()
                .setClientConfiguration(
                        com.chaobo.scm.common.mq.RocketMqClientConfigurations.create(endpoints))
                .setConsumerGroup(group.trim())
                .setSubscriptionExpressions(subscriptions)
                .setConsumptionThreadCount(threads)
                .setMessageListener(this)
                .build();
    }

    private RocketMqPurchaseExternalEventConsumer(
            PurchaseEventEnvelopeCodec codec,
            PurchaseExternalEventHandler handler) {
        this.codec = codec;
        this.handler = handler;
        this.consumer = null;
    }

    static RocketMqPurchaseExternalEventConsumer forTest(
            PurchaseEventEnvelopeCodec codec,
            PurchaseExternalEventHandler handler) {
        return new RocketMqPurchaseExternalEventConsumer(codec, handler);
    }

    @Override
    public ConsumeResult consume(MessageView message) {
        try {
            ByteBuffer body = message.getBody().asReadOnlyBuffer();
            byte[] bytes = new byte[body.remaining()];
            body.get(bytes);
            handler.consume(codec.decode(bytes));
            return ConsumeResult.SUCCESS;
        } catch (Exception exception) {
            LOG.warn(
                    "采购 RocketMQ 业务事件消费失败，等待 Broker 重试，messageId={}, "
                            + "topic={}, deliveryAttempt={}",
                    message.getMessageId(),
                    message.getTopic(),
                    message.getDeliveryAttempt(),
                    exception
            );
            return ConsumeResult.FAILURE;
        }
    }

    private static Map<String, FilterExpression> subscriptions(String topics) {
        if (topics == null) {
            throw new IllegalArgumentException("采购 RocketMQ 消费 Topic 不能为空");
        }
        Map<String, FilterExpression> result = Arrays.stream(topics.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .distinct()
                .collect(Collectors.toUnmodifiableMap(
                        Function.identity(),
                        value -> FilterExpression.SUB_ALL
                ));
        if (result.isEmpty()) {
            throw new IllegalArgumentException("采购 RocketMQ 消费 Topic 不能为空");
        }
        return result;
    }

    @PreDestroy
    public void close() throws Exception {
        if (consumer != null) {
            consumer.close();
        }
    }
}
