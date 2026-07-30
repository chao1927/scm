package com.chaobo.scm.supplier.infrastructure.mq;

import com.chaobo.scm.supplier.application.outbox.*;
import jakarta.annotation.PreDestroy;
import org.apache.rocketmq.client.apis.*;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;

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
     * 创建 RocketMqMessageBrokerAdapter。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param endpoints 业务处理参数或成员，类型为 {@code String}
     * @param topic 业务处理参数或成员，类型为 {@code String}
     */
    public RocketMqMessageBrokerAdapter(@Value("${scm.rocketmq.endpoints}") String endpoints, @Value("${scm.rocketmq.topic:supplier-domain-event}") String topic) throws ClientException {
        this.topic = topic;
        var config = ClientConfiguration.newBuilder().setEndpoints(endpoints).build();
        this.producer = provider.newProducerBuilder().setClientConfiguration(config).setTopics(topic).build();
    }

    /**
     * 执行命令 {@code publish}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param m 业务处理参数或成员，类型为 {@code OutboxMessage}
     */
    public void publish(OutboxMessage m) throws ClientException {
        var message = provider.newMessageBuilder().setTopic(topic).setTag(m.eventType()).setKeys(m.eventCode()).setBody(m.payloadJson().getBytes(StandardCharsets.UTF_8)).build();
        producer.send(message);
    }

    /**
     * 执行命令 {@code close}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @PreDestroy
    public void close() throws Exception {
        producer.close();
    }
}
