package com.chaobo.scm.mdm.infrastructure.mq;

import com.chaobo.scm.mdm.application.outbox.MdmMessageBrokerPort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import jakarta.annotation.PreDestroy;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 主数据真实 RocketMQ Producer，生产环境不提供空实现或日志降级。
 *
 * @author SCM Team
 */
@Component
@Profile("!test")
public class RocketMqMdmMessageBrokerAdapter implements MdmMessageBrokerPort {

    private final ClientServiceProvider provider = ClientServiceProvider.loadService();
    private final Producer producer;
    private final Set<String> configuredTopics;
    private final ObjectMapper objectMapper;

    public RocketMqMdmMessageBrokerAdapter(
            ObjectMapper objectMapper,
            @Value("${scm.rocketmq.endpoints}") String endpoints,
            @Value("${scm.rocketmq.mdm-producer.topics:mdm-domain-event}")
            String topics) throws ClientException {
        this.objectMapper = objectMapper;
        configuredTopics = new LinkedHashSet<>(Arrays.stream(topics.split(","))
            .map(String::trim).filter(value -> !value.isEmpty()).toList());
        if (configuredTopics.isEmpty()) {
            throw new IllegalArgumentException("MDM RocketMQ producer topics are empty");
        }
        producer = provider.newProducerBuilder()
            .setClientConfiguration(
                com.chaobo.scm.common.mq.RocketMqClientConfigurations.create(endpoints))
            .setTopics(configuredTopics.toArray(String[]::new)).build();
    }

    @Override
    public void publish(OutboundMessage message) {
        if (!configuredTopics.contains(message.destinationTopic())) {
            throw new IllegalArgumentException(
                "MDM RocketMQ topic is not configured: " + message.destinationTopic());
        }
        try {
            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("schemaVersion", 1);
            envelope.put("sourceSystem", "MDM");
            envelope.put("eventCode", message.eventCode());
            envelope.put("eventType", message.eventType());
            envelope.put("aggregateNo", message.businessNo());
            envelope.put("businessNo", message.businessNo());
            envelope.put("data", payload(message.payload()));
            producer.send(provider.newMessageBuilder().setTopic(message.destinationTopic())
                .setKeys(message.eventCode()).setTag(message.eventType())
                .setBody(objectMapper.writeValueAsBytes(envelope)).build());
        } catch (Exception exception) {
            throw new IllegalStateException("MDM event RocketMQ delivery failed", exception);
        }
    }

    private JsonNode payload(String value) {
        try {
            return objectMapper.readTree(value);
        } catch (Exception ignored) {
            return TextNode.valueOf(value);
        }
    }

    @PreDestroy
    public void close() throws Exception {
        producer.close();
    }
}
