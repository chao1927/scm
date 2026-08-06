package com.chaobo.scm.supplier.infrastructure.mq;

import com.chaobo.scm.supplier.application.contract.*;
import jakarta.annotation.PreDestroy;
import org.apache.rocketmq.client.apis.*;
import org.apache.rocketmq.client.apis.consumer.*;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * RocketMqContractApprovalConsumer。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Component
@ConditionalOnProperty(name = "scm.rocketmq.contract-approval-consumer.enabled", havingValue = "true")
public class RocketMqContractApprovalConsumer {

    /**
     * log（类型：{@code Logger}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    private static final Logger log = LoggerFactory.getLogger(RocketMqContractApprovalConsumer.class);

    /**
     * consumer（类型：{@code PushConsumer}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final PushConsumer consumer;

    /**
     * service（类型：{@code ContractApprovalEventConsumerApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final ContractApprovalEventConsumerApplicationService service;

    /**
     * json（类型：{@code ObjectMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final ObjectMapper json;

    /**
     * 创建 RocketMqContractApprovalConsumer。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code ContractApprovalEventConsumerApplicationService}
     * @param json 业务处理参数或成员，类型为 {@code ObjectMapper}
     * @param endpoints 业务处理参数或成员，类型为 {@code String}
     * @param topic 业务处理参数或成员，类型为 {@code String}
     * @param group 业务处理参数或成员，类型为 {@code String}
     */
    public RocketMqContractApprovalConsumer(ContractApprovalEventConsumerApplicationService service, ObjectMapper json, @Value("${scm.rocketmq.endpoints}") String endpoints, @Value("${scm.rocketmq.contract-approval-consumer.topic}") String topic, @Value("${scm.rocketmq.contract-approval-consumer.group}") String group) throws Exception {
        this.service = service;
        this.json = json;
        var provider = ClientServiceProvider.loadService();
        consumer = provider.newPushConsumerBuilder().setClientConfiguration(com.chaobo.scm.common.mq.RocketMqClientConfigurations.create(endpoints)).setConsumerGroup(group).setSubscriptionExpressions(Map.of(topic, FilterExpression.SUB_ALL)).setConsumptionThreadCount(4).setMessageListener(this::consume).build();
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
            service.consume(event(message));
            return ConsumeResult.SUCCESS;
        } catch (Exception ex) {
            log.warn("合同审批事件消费失败，等待RocketMQ重试，messageId={}", message.getMessageId(), ex);
            return ConsumeResult.FAILURE;
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code event}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param message 业务处理参数或成员，类型为 {@code MessageView}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ContractApprovalEvent}
     */
    @SuppressWarnings("unchecked")
    private ContractApprovalEvent event(MessageView message) throws Exception {
        var buffer = message.getBody().asReadOnlyBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        Map<String, Object> envelope = json.readValue(bytes, Map.class);
        Map<String, Object> data = envelope.get("data") instanceof Map<?, ?> raw ? (Map<String, Object>) raw : envelope;
        String code = text(envelope, "eventCode", message.getMessageId().toString());
        String type = text(envelope, "eventType", message.getTag().orElse("ContractApprovalCompleted"));
        return new ContractApprovalEvent(code, text(envelope, "sourceSystem", "IAM"), type, number(data.get("contractId")), Math.toIntExact(number(data.get("contractVersion"))), Boolean.parseBoolean(String.valueOf(data.get("approved"))), text(data, "comment", null));
    }

    /**
     * 处理当前类型职责中的操作 {@code text}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param data 业务处理参数或成员，类型为 {@code Map<String,Object>}
     * @param key 业务处理参数或成员，类型为 {@code String}
     * @param fallback 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String text(Map<String, Object> data, String key, String fallback) {
        var v = data.get(key);
        return v == null ? fallback : String.valueOf(v);
    }

    /**
     * 处理当前类型职责中的操作 {@code number}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code Object}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    private static long number(Object value) {
        if (value instanceof Number n) {
            return n.longValue();
        }
        return Long.parseLong(String.valueOf(value));
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
