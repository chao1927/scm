package com.chaobo.scm.purchase.infrastructure.persistence.integration;

import com.chaobo.scm.purchase.application.integration.InboundEventLogPort;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * MyBatisInboundEventLogAdapter。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisInboundEventLogAdapter implements InboundEventLogPort {

    /**
     * mapper（类型：{@code InboundEventLogMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final InboundEventLogMapper mapper;

    /**
     * 创建 MyBatisInboundEventLogAdapter。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code InboundEventLogMapper}
     */
    public MyBatisInboundEventLogAdapter(InboundEventLogMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 处理当前类型职责中的操作 {@code claim}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @param eventType 业务处理参数或成员，类型为 {@code String}
     * @param consumerName 业务处理参数或成员，类型为 {@code String}
     * @param idempotentKey 业务或技术标识，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ClaimResult}
     */
    @Override
    public ClaimResult claim(String sourceSystem, String eventCode, String eventType, String consumerName, String idempotentKey) {
        if (mapper.insertProcessing(sourceSystem, eventCode, eventType, consumerName, idempotentKey) == 1) {
            return ClaimResult.CLAIMED;
        }
        var existing = mapper.find(sourceSystem, eventCode, consumerName);
        if (existing == null) {
            return ClaimResult.IN_PROGRESS;
        }
        if (existing.status() == CLAIM_VALUE_2 || existing.status() == CLAIM_VALUE_4) {
            return ClaimResult.ALREADY_SUCCEEDED;
        }
        if (existing.status() == CLAIM_VALUE_3 && mapper.retryFailed(sourceSystem, eventCode, consumerName) == 1) {
            return ClaimResult.CLAIMED;
        }
        return ClaimResult.IN_PROGRESS;
    }

    /**
     * 执行命令 {@code savePayload}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @param consumerName 业务处理参数或成员，类型为 {@code String}
     * @param payloadJson 业务处理参数或成员，类型为 {@code String}
     */
    @Override
    public void savePayload(String sourceSystem, String eventCode, String consumerName, String payloadJson) {
        mapper.savePayload(sourceSystem, eventCode, consumerName, payloadJson);
    }

    /**
     * 处理当前类型职责中的操作 {@code markSucceeded}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @param consumerName 业务处理参数或成员，类型为 {@code String}
     * @param ignored 业务处理参数或成员，类型为 {@code boolean}
     */
    @Override
    public void markSucceeded(String sourceSystem, String eventCode, String consumerName, boolean ignored) {
        mapper.markSucceeded(sourceSystem, eventCode, consumerName, ignored ? 4 : 2);
    }

    /**
     * 处理当前类型职责中的操作 {@code recordFailure}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @param eventType 业务处理参数或成员，类型为 {@code String}
     * @param consumerName 业务处理参数或成员，类型为 {@code String}
     * @param idempotentKey 业务或技术标识，类型为 {@code String}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     */
    @Override
    public void recordFailure(String sourceSystem, String eventCode, String eventType, String consumerName, String idempotentKey, String reason) {
        var safeReason = reason == null ? "未知消费异常" : reason;
        mapper.recordFailure(sourceSystem, eventCode, eventType, consumerName, idempotentKey, safeReason.length() > 1000 ? safeReason.substring(0, 1000) : safeReason);
    }

    /**
     * 查询并返回 {@code findForReplay}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param consumeLogId 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Optional<ReplayEvent>}
     */
    @Override
    public Optional<ReplayEvent> findForReplay(long consumeLogId) {
        return Optional.ofNullable(mapper.findById(consumeLogId)).filter(event -> event.status() == 3);
    }

    /**
     * 处理当前类型职责中的操作 {@code markReplayRequested}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param consumeLogId 业务或技术标识，类型为 {@code long}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     */
    @Override
    public void markReplayRequested(long consumeLogId, long operatorId, String reason) {
        mapper.markReplayRequested(consumeLogId, operatorId, reason);
    }

    /**
     * 业务常量 {@code CLAIM_VALUE_3}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int CLAIM_VALUE_3 = 3;

    /**
     * 业务常量 {@code CLAIM_VALUE_2}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int CLAIM_VALUE_2 = 2;

    /**
     * 业务常量 {@code CLAIM_VALUE_4}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int CLAIM_VALUE_4 = 4;
}
