package com.chaobo.scm.supplier.infrastructure.mq;

import com.chaobo.scm.supplier.application.masterdata.MasterDataEvent;
import com.chaobo.scm.supplier.application.masterdata.MasterDataEventConsumerApplicationService;
import com.chaobo.scm.supplier.application.masterdata.MasterDataEventFailureApplicationService;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.util.Map;

/** 消费主数据事件；失败返回 RocketMQ FAILURE，由 Broker 负责重投。 */
@Component
@ConditionalOnProperty(name = "scm.rocketmq.master-data-consumer.enabled", havingValue = "true")
public class RocketMqMasterDataEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(RocketMqMasterDataEventConsumer.class);
    private final ClientServiceProvider provider = ClientServiceProvider.loadService();
    private final PushConsumer consumer;
    private final MasterDataEventConsumerApplicationService applicationService;
    private final MasterDataEventFailureApplicationService failureService;
    private final ObjectMapper json;

    public RocketMqMasterDataEventConsumer(MasterDataEventConsumerApplicationService applicationService,
                                            MasterDataEventFailureApplicationService failureService,
                                            ObjectMapper json,
                                            @Value("${scm.rocketmq.endpoints}") String endpoints,
                                            @Value("${scm.rocketmq.master-data-consumer.topic:master-data-domain-event}") String topic,
                                            @Value("${scm.rocketmq.master-data-consumer.group:supplier-master-data-snapshot}") String group)
            throws Exception {
        this.applicationService = applicationService;
        this.failureService = failureService;
        this.json = json;
        var configuration = ClientConfiguration.newBuilder().setEndpoints(endpoints).build();
        this.consumer = provider.newPushConsumerBuilder()
                .setClientConfiguration(configuration)
                .setConsumerGroup(group)
                .setSubscriptionExpressions(Map.of(topic, FilterExpression.SUB_ALL))
                .setConsumptionThreadCount(4)
                .setMessageListener(this::consume)
                .build();
    }

    private ConsumeResult consume(MessageView message) {
        try {
            applicationService.consume(toEvent(message));
            return ConsumeResult.SUCCESS;
        } catch (Exception exception) {
            log.warn("主数据事件消费失败，等待消息队列重试，messageId={}", message.getMessageId(), exception);
            try { failureService.recordFailure(toEvent(message), exception.getMessage()); }
            catch (Exception failure) { log.error("主数据事件失败记录写入失败，messageId={}", message.getMessageId(), failure); }
            return ConsumeResult.FAILURE;
        }
    }

    @SuppressWarnings("unchecked")
    private MasterDataEvent toEvent(MessageView message) throws Exception {
        ByteBuffer buffer = message.getBody().asReadOnlyBuffer();
        byte[] body = new byte[buffer.remaining()];
        buffer.get(body);
        Map<String, Object> envelope = json.readValue(body, Map.class);
        Object rawData = envelope.get("data");
        Map<String, Object> data = rawData instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
        String eventCode = text(envelope, "eventCode", message.getMessageId().toString());
        String eventType = text(envelope, "eventType", message.getTag().orElse(null));
        String sourceSystem = text(envelope, "sourceSystem", "MDM");
        long aggregateId = number(envelope.get("aggregateId"));
        long aggregateVersion = number(envelope.get("aggregateVersion"));
        String occurredAt = text(envelope, "occurredAt", null);
        return new MasterDataEvent(eventCode, eventType, sourceSystem, aggregateId, aggregateVersion,
                occurredAt == null ? OffsetDateTime.now() : OffsetDateTime.parse(occurredAt), data);
    }

    private String text(Map<String, Object> values, String key, String fallback) {
        Object value = values.get(key); return value == null ? fallback : value.toString();
    }
    private long number(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number number) return number.longValue();
        return Long.parseLong(value.toString());
    }
    @PreDestroy public void close() throws Exception { consumer.close(); }
}
