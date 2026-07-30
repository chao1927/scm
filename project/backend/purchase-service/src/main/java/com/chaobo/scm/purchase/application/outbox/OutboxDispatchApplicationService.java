package com.chaobo.scm.purchase.application.outbox;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * OutboxDispatchApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
@ConditionalOnProperty(name = "scm.rocketmq.enabled", havingValue = "true")
public class OutboxDispatchApplicationService {

    /**
     * store（类型：{@code OutboxDispatchPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final OutboxDispatchPort store;

    /**
     * broker（类型：{@code MessageBrokerPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final MessageBrokerPort broker;

    /**
     * 创建 OutboxDispatchApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param store 业务处理参数或成员，类型为 {@code OutboxDispatchPort}
     * @param broker 业务处理参数或成员，类型为 {@code MessageBrokerPort}
     */
    public OutboxDispatchApplicationService(OutboxDispatchPort store, MessageBrokerPort broker) {
        this.store = store;
        this.broker = broker;
    }

    /**
     * 处理当前类型职责中的操作 {@code claim}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param batchSize 业务处理参数或成员，类型为 {@code int}
     * @param maxRetries 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<OutboxMessage>}
     */
    @Transactional(rollbackFor = Exception.class)
    public List<OutboxMessage> claim(int batchSize, int maxRetries) {
        return store.claim(batchSize, maxRetries);
    }

    /**
     * 执行命令 {@code dispatch}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param message 业务处理参数或成员，类型为 {@code OutboxMessage}
     */
    public void dispatch(OutboxMessage message) {
        try {
            broker.publish(message);
            store.markPublished(message.eventId());
        } catch (RuntimeException exception) {
            store.markFailed(message.eventId(), abbreviate(exception.getMessage()));
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code abbreviate}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String abbreviate(String value) {
        if (value == null || value.isBlank()) {
            return "未知投递异常";
        }
        return value.length() <= 1000 ? value : value.substring(0, 1000);
    }
}
