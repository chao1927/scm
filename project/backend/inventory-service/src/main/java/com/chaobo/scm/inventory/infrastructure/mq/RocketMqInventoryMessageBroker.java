package com.chaobo.scm.inventory.infrastructure.mq;

import com.chaobo.scm.inventory.application.InventoryMessageBroker;
import com.chaobo.scm.inventory.application.InventoryOutboxMessage;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 库存领域事件真实 RocketMQ 生产适配器。
 *
 * <p>该 Bean 在非测试环境强制创建；端点不可用会使服务启动或投递失败，不存在内存代理和 Noop 降级。
 *
 * @author SCM Team
 */
@Component
@Profile("!test")
public class RocketMqInventoryMessageBroker implements InventoryMessageBroker {

    private final ClientServiceProvider provider = ClientServiceProvider.loadService();
    private final Producer producer;
    private final String topic;

    public RocketMqInventoryMessageBroker(
            @Value("${scm.rocketmq.endpoints}") String endpoints,
            @Value("${scm.rocketmq.inventory-topic:inventory-domain-event}") String topic)
            throws ClientException {
        this.topic = topic;
        ClientConfiguration configuration =
                com.chaobo.scm.common.mq.RocketMqClientConfigurations.create(endpoints);
        producer = provider.newProducerBuilder()
                .setClientConfiguration(configuration)
                .setTopics(topic)
                .build();
    }

    @Override
    public void publish(InventoryOutboxMessage message) {
        try {
            producer.send(provider.newMessageBuilder()
                    .setTopic(topic)
                    .setKeys(message.eventCode())
                    .setTag(message.eventType())
                    .setBody(message.envelopeJson().getBytes(StandardCharsets.UTF_8))
                    .build());
        } catch (ClientException exception) {
            throw new IllegalStateException(
                    "库存事件 RocketMQ 投递失败: " + exception.getMessage(),
                    exception);
        }
    }

    /**
     * 释放 RocketMQ 生产者连接。
     *
     * @throws Exception 客户端关闭异常
     */
    @PreDestroy
    public void close() throws Exception {
        producer.close();
    }
}
