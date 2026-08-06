package com.chaobo.scm.iam.infrastructure.mq;

import com.chaobo.scm.iam.application.outbox.IamMessageBrokerPort;
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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * IAM 真实 RocketMQ Producer，生产环境不提供空实现或日志降级。
 *
 * @author SCM Team
 */
@Component
@Profile("!test")
public class RocketMqIamMessageBrokerAdapter implements IamMessageBrokerPort {

    private final ClientServiceProvider provider = ClientServiceProvider.loadService();
    private final Producer producer;
    private final String topic;
    private final ObjectMapper objectMapper;

    public RocketMqIamMessageBrokerAdapter(
            ObjectMapper objectMapper,
            @Value("${scm.rocketmq.endpoints}") String endpoints,
            @Value("${scm.rocketmq.iam-topic:iam-domain-event}") String topic)
            throws ClientException {
        this.objectMapper = objectMapper;
        this.topic = topic;
        producer = provider.newProducerBuilder()
            .setClientConfiguration(
                com.chaobo.scm.common.mq.RocketMqClientConfigurations.create(endpoints))
            .setTopics(topic).build();
    }

    @Override
    public void publish(OutboundMessage message) {
        try {
            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("schemaVersion", 1);
            envelope.put("sourceSystem", "IAM");
            envelope.put("eventCode", message.eventCode());
            envelope.put("eventType", message.eventType());
            envelope.put("aggregateNo", message.businessNo());
            envelope.put("businessNo", message.businessNo());
            envelope.put("data", payload(message.payload()));
            producer.send(provider.newMessageBuilder().setTopic(topic)
                .setKeys(message.eventCode()).setTag(message.eventType())
                .setBody(objectMapper.writeValueAsBytes(envelope)).build());
        } catch (Exception exception) {
            throw new IllegalStateException("IAM event RocketMQ delivery failed", exception);
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
