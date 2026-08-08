package com.chaobo.scm.inventory.infrastructure.mq;

import com.chaobo.scm.inventory.application.InventoryEventEnvelope;
import com.chaobo.scm.common.logging.ScmLogContext;
import com.chaobo.scm.inventory.application.InventoryEventEnvelopeCodec;
import com.chaobo.scm.inventory.application.InventoryInboundEventApplicationService;
import jakarta.annotation.PreDestroy;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.consumer.FilterExpression;
import org.apache.rocketmq.client.apis.consumer.PushConsumer;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 外部领域事件真实 RocketMQ Listener。
 *
 * <p>Listener 只做消息字节到标准信封的协议转换，随后调用 Inbox/版本化应用服务。任何解析、版本、
 * 乱序或记账失败均返回 {@link ConsumeResult#FAILURE}，由 RocketMQ 负责再次投递。
 *
 * @author SCM Team
 */
@Component
@Profile("!test")
public class RocketMqInventoryEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(RocketMqInventoryEventListener.class);
    private static final String TOPIC_SEPARATOR = ",";

    private final ClientServiceProvider provider = ClientServiceProvider.loadService();
    private final InventoryEventEnvelopeCodec codec;
    private final InventoryInboundEventApplicationService inbound;
    private final PushConsumer consumer;

    public RocketMqInventoryEventListener(
            InventoryEventEnvelopeCodec codec,
            InventoryInboundEventApplicationService inbound,
            @Value("${scm.rocketmq.endpoints}") String endpoints,
            @Value("${scm.rocketmq.inbound-topics}") String configuredTopics,
            @Value("${scm.rocketmq.consumer-group:inventory-domain-event-consumer}")
                    String consumerGroup)
            throws Exception {
        this.codec = codec;
        this.inbound = inbound;
        ClientConfiguration configuration =
                com.chaobo.scm.common.mq.RocketMqClientConfigurations.create(endpoints);
        consumer = provider.newPushConsumerBuilder()
                .setClientConfiguration(configuration)
                .setConsumerGroup(consumerGroup)
                .setSubscriptionExpressions(subscriptions(configuredTopics))
                .setConsumptionThreadCount(4)
                .setMessageListener(this::consume)
                .build();
    }

    private ConsumeResult consume(MessageView message) {
        String body = body(message);
        try (ScmLogContext ignored = ScmLogContext.openSystem(ScmLogContext.reference(message.getMessageId()))) {
            InventoryEventEnvelope event = codec.decode(body);
            inbound.consume(event, body);
            log.info("event=rocketmq_consume operation=inventory_event_consume result=SUCCESS messageId={} topic={}",
                    message.getMessageId(), message.getTopic());
            return ConsumeResult.SUCCESS;
        } catch (RuntimeException exception) {
            try (ScmLogContext ignored = ScmLogContext.openSystem(ScmLogContext.reference(message.getMessageId()))) {
                log.warn(
                        "event=rocketmq_consume operation=inventory_event_consume result=RETRY messageId={} topic={}",
                        message.getMessageId(), message.getTopic(), exception);
            }
            return ConsumeResult.FAILURE;
        }
    }

    private static Map<String, FilterExpression> subscriptions(String configuredTopics) {
        Map<String, FilterExpression> subscriptions = new LinkedHashMap<>();
        for (String value : configuredTopics.split(TOPIC_SEPARATOR)) {
            String topic = value.trim();
            if (!topic.isEmpty()) {
                subscriptions.put(topic, FilterExpression.SUB_ALL);
            }
        }
        if (subscriptions.isEmpty()) {
            throw new IllegalArgumentException("库存 RocketMQ 入站 Topic 不能为空");
        }
        return subscriptions;
    }

    private static String body(MessageView message) {
        ByteBuffer buffer = message.getBody().asReadOnlyBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * 释放 RocketMQ 消费者连接。
     *
     * @throws Exception 客户端关闭异常
     */
    @PreDestroy
    public void close() throws Exception {
        consumer.close();
    }
}
