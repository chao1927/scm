package com.chaobo.scm.purchase.infrastructure.persistence.event;

import com.chaobo.scm.purchase.application.outbox.OutboxDispatchPort;
import com.chaobo.scm.purchase.application.outbox.OutboxMessage;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * MyBatisOutboxDispatchAdapter。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisOutboxDispatchAdapter implements OutboxDispatchPort {

    /**
     * mapper（类型：{@code EventPersistenceMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final EventPersistenceMapper mapper;

    /**
     * 创建 MyBatisOutboxDispatchAdapter。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code EventPersistenceMapper}
     */
    public MyBatisOutboxDispatchAdapter(EventPersistenceMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 处理当前类型职责中的操作 {@code claim}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param batchSize 业务处理参数或成员，类型为 {@code int}
     * @param maxRetries 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<OutboxMessage>}
     */
    @Override
    public List<OutboxMessage> claim(int batchSize, int maxRetries) {
        var messages = mapper.claimOutbox(batchSize, maxRetries);
        if (!messages.isEmpty()) {
            mapper.markOutboxPublishing(messages.stream().map(OutboxMessage::eventId).toList());
        }
        return messages;
    }

    /**
     * 处理当前类型职责中的操作 {@code markPublished}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param eventId 业务或技术标识，类型为 {@code long}
     */
    @Override
    public void markPublished(long eventId) {
        mapper.markOutboxPublished(eventId);
    }

    /**
     * 处理当前类型职责中的操作 {@code markFailed}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param eventId 业务或技术标识，类型为 {@code long}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     */
    @Override
    public void markFailed(long eventId, String reason) {
        mapper.markOutboxFailed(eventId, reason);
    }
}
