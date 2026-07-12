package com.chaobo.scm.purchase.infrastructure.mq;

import com.chaobo.scm.purchase.application.outbox.MessageBrokerPort;
import com.chaobo.scm.purchase.application.outbox.OutboxMessage;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@ConditionalOnProperty(name = "scm.rocketmq.enabled", havingValue = "true")
public class RocketMqMessageBrokerAdapter implements MessageBrokerPort {
    private final ClientServiceProvider provider = ClientServiceProvider.loadService();
    private final Producer producer;
    private final String topic;

    public RocketMqMessageBrokerAdapter(@Value("${scm.rocketmq.endpoints}") String endpoints,
                                        @Value("${scm.rocketmq.purchase-topic:purchase-domain-event}") String topic)
            throws ClientException {
        this.topic = topic;
        var configuration = ClientConfiguration.newBuilder().setEndpoints(endpoints).build();
        this.producer = provider.newProducerBuilder()
                .setClientConfiguration(configuration)
                .setTopics(topic)
                .build();
    }

    @Override
    public void publish(OutboxMessage message) {
        try {
            var mqMessage = provider.newMessageBuilder()
                    .setTopic(topic)
                    .setKeys(message.eventCode())
                    .setTag(message.eventType())
                    .setBody(message.payloadJson().getBytes(StandardCharsets.UTF_8))
                    .build();
            producer.send(mqMessage);
        } catch (ClientException exception) {
            throw new IllegalStateException("采购事件投递失败: " + exception.getMessage(), exception);
        }
    }
}
