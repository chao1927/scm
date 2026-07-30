package com.chaobo.scm.tms.infrastructure.mq;

import com.chaobo.scm.tms.application.TmsInboundEventApplicationService;
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
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * TMS 跨上下文业务事件真实 RocketMQ 消费者。
 *
 * @author SCM Team
 */
@Component
@Profile("!test")
public class RocketMqTmsInboundEventConsumer {

    private static final Logger LOG =
        LoggerFactory.getLogger(RocketMqTmsInboundEventConsumer.class);
    private final PushConsumer consumer;
    private final ObjectMapper objectMapper;
    private final TmsInboundEventApplicationService application;

    public RocketMqTmsInboundEventConsumer(
            ObjectMapper objectMapper,
            TmsInboundEventApplicationService application,
            @Value("${scm.rocketmq.endpoints}") String endpoints,
            @Value("${scm.rocketmq.tms-consumer.topics:"
                + "oms-domain-event,wms-domain-event,purchase-domain-event,"
                + "supplier-domain-event,master-data-domain-event,bms-domain-event}")
            String topics,
            @Value("${scm.rocketmq.tms-consumer.group:tms-business-event-consumer}")
            String group) throws Exception {
        this.objectMapper = objectMapper;
        this.application = application;
        var subscriptions = Arrays.stream(topics.split(","))
            .map(String::trim).filter(value -> !value.isEmpty())
            .collect(Collectors.toUnmodifiableMap(
                value -> value, value -> FilterExpression.SUB_ALL));
        if (subscriptions.isEmpty()) {
            throw new IllegalArgumentException("TMS RocketMQ consumer topics are empty");
        }
        consumer = ClientServiceProvider.loadService().newPushConsumerBuilder()
            .setClientConfiguration(
                ClientConfiguration.newBuilder().setEndpoints(endpoints).build())
            .setConsumerGroup(group)
            .setSubscriptionExpressions(subscriptions)
            .setConsumptionThreadCount(4)
            .setMessageListener(this::consume)
            .build();
    }

    private ConsumeResult consume(MessageView message) {
        try {
            JsonNode root = read(message);
            int version = required(root, "schemaVersion").asInt();
            if (version != 1) {
                throw new IllegalArgumentException(
                    "unsupported TMS event envelope version: " + version);
            }
            application.consume(new TmsInboundEventApplicationService.EventEnvelope(
                text(root, "sourceSystem"), text(root, "eventCode"),
                text(root, "eventType"), optionalText(root, "aggregateNo"),
                required(root, "data")));
            return ConsumeResult.SUCCESS;
        } catch (Exception exception) {
            LOG.warn("TMS RocketMQ event failed; waiting for broker retry, messageId={}",
                message.getMessageId(), exception);
            return ConsumeResult.FAILURE;
        }
    }

    private JsonNode read(MessageView message) throws Exception {
        var buffer = message.getBody().asReadOnlyBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return objectMapper.readTree(bytes);
    }

    private static JsonNode required(JsonNode root, String name) {
        JsonNode value = root.get(name);
        if (value == null || value.isNull()) {
            throw new IllegalArgumentException("event envelope field missing: " + name);
        }
        return value;
    }

    private static String text(JsonNode root, String name) {
        String value = required(root, name).asText();
        if (value.isBlank()) {
            throw new IllegalArgumentException("event envelope field is blank: " + name);
        }
        return value;
    }

    private static String optionalText(JsonNode root, String name) {
        JsonNode value = root.get(name);
        return value == null || value.isNull() ? null : value.asText();
    }

    @PreDestroy
    public void close() throws Exception {
        consumer.close();
    }
}
