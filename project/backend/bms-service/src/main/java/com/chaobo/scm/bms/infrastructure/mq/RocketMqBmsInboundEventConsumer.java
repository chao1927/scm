package com.chaobo.scm.bms.infrastructure.mq;

import com.chaobo.scm.common.logging.ScmLogContext;
import com.chaobo.scm.bms.application.BmsApplicationService;
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
 * BMS 业务事件真实 RocketMQ Consumer。
 *
 * @author SCM Team
 */
@Component
@Profile("!test")
public class RocketMqBmsInboundEventConsumer {

    private static final Logger LOG =
        LoggerFactory.getLogger(RocketMqBmsInboundEventConsumer.class);
    private static final String SCHEMA_VERSION_FIELD = "schemaVersion";
    private final ObjectMapper objectMapper;
    private final BmsApplicationService application;
    private final PushConsumer consumer;

    public RocketMqBmsInboundEventConsumer(
            ObjectMapper objectMapper, BmsApplicationService application,
            @Value("${scm.rocketmq.endpoints}") String endpoints,
            @Value("${scm.rocketmq.bms-consumer.topics:"
                + "purchase-domain-event,supplier-domain-event,wms-domain-event,"
                + "tms-domain-event,oms-domain-event}") String topics,
            @Value("${scm.rocketmq.bms-consumer.group:bms-business-event-consumer}")
            String group) throws Exception {
        this.objectMapper = objectMapper;
        this.application = application;
        var subscriptions = Arrays.stream(topics.split(","))
            .map(String::trim).filter(value -> !value.isEmpty())
            .collect(Collectors.toUnmodifiableMap(
                value -> value, value -> FilterExpression.SUB_ALL));
        if (subscriptions.isEmpty()) {
            throw new IllegalArgumentException("BMS RocketMQ consumer topics are empty");
        }
        consumer = ClientServiceProvider.loadService().newPushConsumerBuilder()
            .setClientConfiguration(
                com.chaobo.scm.common.mq.RocketMqClientConfigurations.create(endpoints))
            .setConsumerGroup(group)
            .setSubscriptionExpressions(subscriptions)
            .setConsumptionThreadCount(4)
            .setMessageListener(this::consume)
            .build();
    }

    private ConsumeResult consume(MessageView message) {
        try (ScmLogContext ignored = ScmLogContext.openSystem(ScmLogContext.reference(message.getMessageId()))) {
            JsonNode root = read(message);
            if (required(root, SCHEMA_VERSION_FIELD).asInt() != 1) {
                throw new IllegalArgumentException(
                    "unsupported BMS event envelope version");
            }
            application.consumeEvent(new BmsApplicationService.ConsumeEventCommand(
                text(root, "sourceSystem"), text(root, "eventCode"),
                text(root, "eventType"), businessNo(root),
                required(root, "data").toString()));
            LOG.info("event=rocketmq_consume operation=bms_inbound_event_consume result=SUCCESS messageId={} topic={}",
                message.getMessageId(), message.getTopic());
            return ConsumeResult.SUCCESS;
        } catch (Exception exception) {
            try (ScmLogContext ignored = ScmLogContext.openSystem(ScmLogContext.reference(message.getMessageId()))) {
                LOG.warn("event=rocketmq_consume operation=bms_inbound_event_consume result=RETRY messageId={} topic={}",
                    message.getMessageId(), message.getTopic(), exception);
            }
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

    private static String businessNo(JsonNode root) {
        JsonNode value = root.get("businessNo");
        if (value == null || value.isNull() || value.asText().isBlank()) {
            value = root.get("aggregateNo");
        }
        return value == null || value.isNull() ? text(root, "eventCode") : value.asText();
    }

    @PreDestroy
    public void close() throws Exception {
        consumer.close();
    }
}
