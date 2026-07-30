package com.chaobo.scm.wms.infrastructure.mq;

import com.chaobo.scm.wms.application.outbox.WmsMessageBrokerPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * WMS Outbox 的真实 RocketMQ 生产适配器。
 *
 * <p>生产环境不存在日志或内存降级；RocketMQ 未启用时端口没有 Bean，应用将失败关闭。
 */
@Component
@ConditionalOnProperty(name = "scm.rocketmq.enabled", havingValue = "true", matchIfMissing = true)
public class RocketMqWmsMessageBrokerAdapter implements WmsMessageBrokerPort {

    private final ClientServiceProvider provider = ClientServiceProvider.loadService();
    private final Producer producer;
    private final String topic;
    private final ObjectMapper json;

    public RocketMqWmsMessageBrokerAdapter(
            ObjectMapper json,
            @Value("${scm.rocketmq.endpoints}") String endpoints,
            @Value("${scm.rocketmq.wms-topic:wms-domain-event}") String topic)
            throws ClientException {
        this.json = json;
        this.topic = topic;
        this.producer = provider.newProducerBuilder()
            .setClientConfiguration(ClientConfiguration.newBuilder().setEndpoints(endpoints).build())
            .setTopics(topic).build();
    }

    @Override
    public void publish(String eventCode, String eventType, String payload) {
        try {
            var data = json.readTree(payload);
            var envelope = json.writeValueAsBytes(Map.of(
                "schemaVersion", 1,
                "sourceSystem", "WMS",
                "eventCode", eventCode,
                "eventType", eventType,
                "data", data));
            var message = provider.newMessageBuilder().setTopic(topic).setKeys(eventCode)
                .setTag(eventType).setBody(envelope).build();
            producer.send(message);
        } catch (Exception exception) {
            throw new IllegalStateException("WMS 事件投递 RocketMQ 失败", exception);
        }
    }

    @PreDestroy
    public void close() throws Exception {
        producer.close();
    }
}
