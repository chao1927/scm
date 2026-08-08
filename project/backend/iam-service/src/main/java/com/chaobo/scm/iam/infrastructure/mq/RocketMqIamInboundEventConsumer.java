package com.chaobo.scm.iam.infrastructure.mq;

import com.chaobo.scm.common.logging.ScmLogContext;
import com.chaobo.scm.iam.application.IamAdminApplicationService;
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
 * IAM 真实 RocketMQ Consumer，业务成功后由应用服务落 Inbox。
 *
 * @author SCM Team
 */
@Component
@Profile("!test")
public class RocketMqIamInboundEventConsumer {

    private static final String SCHEMA_VERSION_FIELD = "schemaVersion";
    private static final Logger LOG =
        LoggerFactory.getLogger(RocketMqIamInboundEventConsumer.class);
    private final ObjectMapper objectMapper;
    private final IamAdminApplicationService application;
    private final PushConsumer consumer;

    public RocketMqIamInboundEventConsumer(
            ObjectMapper objectMapper, IamAdminApplicationService application,
            @Value("${scm.rocketmq.endpoints}") String endpoints,
            @Value("${scm.rocketmq.iam-consumer.topics}") String topics,
            @Value("${scm.rocketmq.iam-consumer.group:iam-business-event-consumer}")
            String group) throws Exception {
        this.objectMapper = objectMapper;
        this.application = application;
        var subscriptions = Arrays.stream(topics.split(","))
            .map(String::trim).filter(value -> !value.isEmpty())
            .collect(Collectors.toUnmodifiableMap(
                value -> value, value -> FilterExpression.SUB_ALL));
        if (subscriptions.isEmpty()) {
            throw new IllegalArgumentException("IAM RocketMQ consumer topics are empty");
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
            application.consumeEvent(toEvent(root));
            LOG.info("event=rocketmq_consume operation=iam_inbound_event_consume result=SUCCESS messageId={} topic={}",
                message.getMessageId(), message.getTopic());
            return ConsumeResult.SUCCESS;
        } catch (Exception exception) {
            try (ScmLogContext ignored = ScmLogContext.openSystem(ScmLogContext.reference(message.getMessageId()))) {
                LOG.warn("event=rocketmq_consume operation=iam_inbound_event_consume result=RETRY messageId={} topic={}",
                    message.getMessageId(), message.getTopic(), exception);
            }
            return ConsumeResult.FAILURE;
        }
    }

    static IamAdminApplicationService.EventEnvelope toEvent(JsonNode root) {
        if (required(root, SCHEMA_VERSION_FIELD).asInt() != 1) {
            throw new IllegalArgumentException("unsupported IAM event envelope version");
        }
        return new IamAdminApplicationService.EventEnvelope(
            text(root, "eventCode"), text(root, "eventType"),
            text(root, "sourceSystem"), businessNo(root), required(root, "data").toString());
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
        return value == null || value.isNull()
            ? text(root, "eventCode") : value.asText();
    }

    @PreDestroy
    public void close() throws Exception {
        consumer.close();
    }
}
