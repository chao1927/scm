package com.chaobo.scm.wms.infrastructure.mq;

import com.chaobo.scm.wms.application.inbox.WmsInboundEventApplicationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * WMS 业务事件真实 RocketMQ 消费者。
 *
 * <p>消息先进入已有 Inbox 幂等表再分发。任何解析、版本或业务异常都返回 FAILURE，
 * 由 RocketMQ 执行 Broker 级重投，绝不吞掉消息或切换到 HTTP 模拟消费。
 */
@Component
@ConditionalOnProperty(name = "scm.rocketmq.enabled", havingValue = "true", matchIfMissing = true)
public class RocketMqWmsInboundEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(RocketMqWmsInboundEventConsumer.class);
    private final PushConsumer consumer;
    private final WmsInboundEventApplicationService application;
    private final ObjectMapper json;

    public RocketMqWmsInboundEventConsumer(
            WmsInboundEventApplicationService application,
            ObjectMapper json,
            @Value("${scm.rocketmq.endpoints}") String endpoints,
            @Value("${scm.rocketmq.wms-consumer.topics:"
                + "purchase-domain-event,oms-domain-event,inventory-domain-event,supplier-domain-event}")
            String topics,
            @Value("${scm.rocketmq.wms-consumer.group:wms-business-event-consumer}") String group)
            throws Exception {
        this.application = application;
        this.json = json;
        var subscriptions = Arrays.stream(topics.split(",")).map(String::trim)
            .filter(value -> !value.isEmpty())
            .collect(Collectors.toUnmodifiableMap(value -> value, value -> FilterExpression.SUB_ALL));
        if (subscriptions.isEmpty()) {
            throw new IllegalArgumentException("WMS RocketMQ 消费 Topic 不能为空");
        }
        this.consumer = ClientServiceProvider.loadService().newPushConsumerBuilder()
            .setClientConfiguration(ClientConfiguration.newBuilder().setEndpoints(endpoints).build())
            .setConsumerGroup(group).setSubscriptionExpressions(subscriptions)
            .setConsumptionThreadCount(4).setMessageListener(this::consume).build();
    }

    private ConsumeResult consume(MessageView message) {
        try {
            var root = read(message);
            int version = required(root, "schemaVersion").asInt();
            if (version != 1) {
                throw new IllegalArgumentException("不支持的事件信封版本: " + version);
            }
            var envelope = new WmsInboundEventApplicationService.EventEnvelope(
                text(root, "sourceSystem"), text(root, "eventCode"),
                text(root, "eventType"), required(root, "data").toString());
            application.consume(envelope, 0L);
            return ConsumeResult.SUCCESS;
        } catch (Exception exception) {
            LOG.warn("WMS RocketMQ 事件消费失败，等待 Broker 重试，messageId={}",
                message.getMessageId(), exception);
            return ConsumeResult.FAILURE;
        }
    }

    private JsonNode read(MessageView message) throws Exception {
        var buffer = message.getBody().asReadOnlyBuffer();
        var bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return json.readTree(bytes);
    }

    private static JsonNode required(JsonNode root, String name) {
        var value = root.get(name);
        if (value == null || value.isNull()) {
            throw new IllegalArgumentException("事件信封缺少字段: " + name);
        }
        return value;
    }

    private static String text(JsonNode root, String name) {
        var value = required(root, name).asText();
        if (value.isBlank()) {
            throw new IllegalArgumentException("事件信封字段不能为空: " + name);
        }
        return value;
    }

    @PreDestroy
    public void close() throws Exception {
        consumer.close();
    }
}
