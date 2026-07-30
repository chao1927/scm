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

/**
 * 消费主数据事件；失败返回 RocketMQ FAILURE，由 Broker 负责重投。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Component
@ConditionalOnProperty(name = "scm.rocketmq.master-data-consumer.enabled", havingValue = "true")
public class RocketMqMasterDataEventConsumer {

    /**
     * log（类型：{@code Logger}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    private static final Logger log = LoggerFactory.getLogger(RocketMqMasterDataEventConsumer.class);

    /**
     * provider（类型：{@code ClientServiceProvider}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final ClientServiceProvider provider = ClientServiceProvider.loadService();

    /**
     * consumer（类型：{@code PushConsumer}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final PushConsumer consumer;

    /**
     * applicationService（类型：{@code MasterDataEventConsumerApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final MasterDataEventConsumerApplicationService applicationService;

    /**
     * failureService（类型：{@code MasterDataEventFailureApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final MasterDataEventFailureApplicationService failureService;

    /**
     * json（类型：{@code ObjectMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final ObjectMapper json;

    /**
     * 创建 RocketMqMasterDataEventConsumer。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param applicationService 应用或外部协作依赖，类型为 {@code MasterDataEventConsumerApplicationService}
     * @param failureService 应用或外部协作依赖，类型为 {@code MasterDataEventFailureApplicationService}
     * @param json 业务处理参数或成员，类型为 {@code ObjectMapper}
     * @param endpoints 业务处理参数或成员，类型为 {@code String}
     * @param topic 业务处理参数或成员，类型为 {@code String}
     * @param group 业务处理参数或成员，类型为 {@code String}
     */
    public RocketMqMasterDataEventConsumer(MasterDataEventConsumerApplicationService applicationService, MasterDataEventFailureApplicationService failureService, ObjectMapper json, @Value("${scm.rocketmq.endpoints}") String endpoints, @Value("${scm.rocketmq.master-data-consumer.topic:master-data-domain-event}") String topic, @Value("${scm.rocketmq.master-data-consumer.group:supplier-master-data-snapshot}") String group) throws Exception {
        this.applicationService = applicationService;
        this.failureService = failureService;
        this.json = json;
        var configuration = ClientConfiguration.newBuilder().setEndpoints(endpoints).build();
        this.consumer = provider.newPushConsumerBuilder().setClientConfiguration(configuration).setConsumerGroup(group).setSubscriptionExpressions(Map.of(topic, FilterExpression.SUB_ALL)).setConsumptionThreadCount(4).setMessageListener(this::consume).build();
    }

    /**
     * 执行命令 {@code consume}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param message 业务处理参数或成员，类型为 {@code MessageView}
     * @return 执行命令的结果，类型为 {@code ConsumeResult}
     */
    private ConsumeResult consume(MessageView message) {
        try {
            applicationService.consume(toEvent(message));
            return ConsumeResult.SUCCESS;
        } catch (Exception exception) {
            log.warn("主数据事件消费失败，等待消息队列重试，messageId={}", message.getMessageId(), exception);
            try {
                failureService.recordFailure(toEvent(message), exception.getMessage());
            } catch (Exception failure) {
                log.error("主数据事件失败记录写入失败，messageId={}", message.getMessageId(), failure);
            }
            return ConsumeResult.FAILURE;
        }
    }

    /**
     * 转换数据模型 {@code toEvent}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param message 业务处理参数或成员，类型为 {@code MessageView}
     * @return 转换数据模型的结果，类型为 {@code MasterDataEvent}
     */
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
        return new MasterDataEvent(eventCode, eventType, sourceSystem, aggregateId, aggregateVersion, occurredAt == null ? OffsetDateTime.now() : OffsetDateTime.parse(occurredAt), data);
    }

    /**
     * 处理当前类型职责中的操作 {@code text}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param values 业务处理参数或成员，类型为 {@code Map<String,Object>}
     * @param key 业务处理参数或成员，类型为 {@code String}
     * @param fallback 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String text(Map<String, Object> values, String key, String fallback) {
        Object value = values.get(key);
        return value == null ? fallback : value.toString();
    }

    /**
     * 处理当前类型职责中的操作 {@code number}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code Object}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    private long number(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    /**
     * 执行命令 {@code close}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @PreDestroy
    public void close() throws Exception {
        consumer.close();
    }
}
