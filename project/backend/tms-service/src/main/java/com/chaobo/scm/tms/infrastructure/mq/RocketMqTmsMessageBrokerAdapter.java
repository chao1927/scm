package com.chaobo.scm.tms.infrastructure.mq;

import com.chaobo.scm.tms.application.outbox.TmsMessageBrokerPort;
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
 * TMS Outbox 的真实 RocketMQ 生产适配器。
 *
 * <p>生产代码没有日志、内存或 Noop 降级。连接及发送失败会回到 Outbox 重试流程。
 *
 * @author SCM Team
 */
@Component
@Profile("!test")
public class RocketMqTmsMessageBrokerAdapter implements TmsMessageBrokerPort {

    private final ClientServiceProvider provider = ClientServiceProvider.loadService();
    private final Producer producer;
    private final String topic;
    private final ObjectMapper objectMapper;

    public RocketMqTmsMessageBrokerAdapter(
            ObjectMapper objectMapper,
            @Value("${scm.rocketmq.endpoints}") String endpoints,
            @Value("${scm.rocketmq.tms-topic:tms-domain-event}") String topic)
            throws ClientException {
        this.objectMapper = objectMapper;
        this.topic = topic;
        this.producer = provider.newProducerBuilder()
            .setClientConfiguration(
                com.chaobo.scm.common.mq.RocketMqClientConfigurations.create(endpoints))
            .setTopics(topic)
            .build();
    }

    @Override
    public void publish(OutboundMessage message) {
        try {
            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("schemaVersion", 1);
            envelope.put("sourceSystem", "TMS");
            envelope.put("eventCode", message.eventCode());
            envelope.put("eventType", message.eventType());
            envelope.put("aggregateNo", message.businessNo());
            envelope.put("data", payload(message.payload()));
            byte[] body = objectMapper.writeValueAsBytes(envelope);
            producer.send(provider.newMessageBuilder()
                .setTopic(topic)
                .setKeys(message.eventCode())
                .setTag(message.eventType())
                .setBody(body)
                .build());
        } catch (Exception exception) {
            throw new IllegalStateException("TMS event RocketMQ delivery failed", exception);
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
