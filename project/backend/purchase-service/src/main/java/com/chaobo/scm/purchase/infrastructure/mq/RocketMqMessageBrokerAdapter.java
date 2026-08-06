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
import jakarta.annotation.PreDestroy;

/**
 * RocketMqMessageBrokerAdapter。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Component
@ConditionalOnProperty(name = "scm.rocketmq.enabled", havingValue = "true")
public class RocketMqMessageBrokerAdapter implements MessageBrokerPort {

    /**
     * provider（类型：{@code ClientServiceProvider}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final ClientServiceProvider provider = ClientServiceProvider.loadService();

    /**
     * producer（类型：{@code Producer}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final Producer producer;

    /**
     * topic（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String topic;

    /**
     * 标准事件信封编解码器。
     */
    private final PurchaseEventEnvelopeCodec codec;

    /**
     * 创建 RocketMqMessageBrokerAdapter。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param codec 标准事件信封编解码器
     * @param endpoints 业务处理参数或成员，类型为 {@code String}
     * @param topic 业务处理参数或成员，类型为 {@code String}
     */
    public RocketMqMessageBrokerAdapter(
            PurchaseEventEnvelopeCodec codec,
            @Value("${scm.rocketmq.endpoints}") String endpoints,
            @Value("${scm.rocketmq.purchase-topic:purchase-domain-event}") String topic)
            throws ClientException {
        this.codec = codec;
        this.topic = topic;
        var configuration =
                com.chaobo.scm.common.mq.RocketMqClientConfigurations.create(endpoints);
        this.producer = provider.newProducerBuilder().setClientConfiguration(configuration).setTopics(topic).build();
    }

    /**
     * 执行命令 {@code publish}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param message 业务处理参数或成员，类型为 {@code OutboxMessage}
     */
    @Override
    public void publish(OutboxMessage message) {
        try {
            var mqMessage = provider.newMessageBuilder().setTopic(topic)
                    .setKeys(message.eventCode())
                    .setTag(message.eventType())
                    .setBody(codec.encode(message))
                    .build();
            producer.send(mqMessage);
        } catch (Exception exception) {
            throw new IllegalStateException("采购事件投递失败: " + exception.getMessage(), exception);
        }
    }

    /**
     * 关闭 RocketMQ 生产者并释放网络资源。
     */
    @PreDestroy
    public void close() throws Exception {
        producer.close();
    }
}
