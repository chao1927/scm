package com.chaobo.scm.bms.infrastructure.mq;

import com.chaobo.scm.bms.application.outbox.BmsMessageBrokerPort;
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
 * BMS 真实 RocketMQ Producer。
 *
 * @author SCM Team
 */
@Component
@Profile("!test")
public class RocketMqBmsMessageBrokerAdapter implements BmsMessageBrokerPort {

    private final ClientServiceProvider provider = ClientServiceProvider.loadService();
    private final Producer producer;
    private final String topic;
    private final ObjectMapper objectMapper;

    public RocketMqBmsMessageBrokerAdapter(
            ObjectMapper objectMapper,
            @Value("${scm.rocketmq.endpoints}") String endpoints,
            @Value("${scm.rocketmq.bms-topic:bms-domain-event}") String topic)
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
            envelope.put("sourceSystem", "BMS");
            envelope.put("eventCode", message.eventCode());
            envelope.put("eventType", message.eventType());
            envelope.put("aggregateNo", message.aggregateNo());
            envelope.put("businessNo", message.businessNo());
            envelope.put("data", payload(message.payload()));
            producer.send(provider.newMessageBuilder().setTopic(topic)
                .setKeys(message.eventCode()).setTag(message.eventType())
                .setBody(objectMapper.writeValueAsBytes(envelope)).build());
        } catch (Exception exception) {
            throw new IllegalStateException("BMS event RocketMQ delivery failed", exception);
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
