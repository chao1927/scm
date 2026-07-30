package com.chaobo.scm.supplier.infrastructure.persistence.masterdata;

import com.chaobo.scm.supplier.application.masterdata.MasterDataEventConsumeLogPort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * MyBatisMasterDataEventConsumeLogAdapter。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisMasterDataEventConsumeLogAdapter implements MasterDataEventConsumeLogPort {

    /**
     * mapper（类型：{@code MasterDataEventConsumeLogMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final MasterDataEventConsumeLogMapper mapper;

    /**
     * 创建 MyBatisMasterDataEventConsumeLogAdapter。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code MasterDataEventConsumeLogMapper}
     */
    public MyBatisMasterDataEventConsumeLogAdapter(MasterDataEventConsumeLogMapper mapper) {
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
        var row = mapper.find(sourceSystem, eventCode, consumerName);
        if (row == null) {
            return ClaimResult.IN_PROGRESS;
        }
        if (row.status() == CLAIM_VALUE_2 || row.status() == CLAIM_VALUE_4) {
            return ClaimResult.ALREADY_SUCCEEDED;
        }
        if (row.status() == CLAIM_VALUE_3 && mapper.retryFailed(sourceSystem, eventCode, consumerName) == 1) {
            return ClaimResult.CLAIMED;
        }
        return ClaimResult.IN_PROGRESS;
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
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void recordFailure(String sourceSystem, String eventCode, String eventType, String consumerName, String idempotentKey, String reason) {
        String safeReason = reason == null ? "未知错误" : reason.substring(0, Math.min(reason.length(), 1000));
        mapper.recordFailure(sourceSystem, eventCode, eventType, consumerName, idempotentKey, safeReason);
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
     * 查询并返回 {@code findForReplay}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code java.util.Optional<ReplayEvent>}
     */
    @Override
    public java.util.Optional<ReplayEvent> findForReplay(long id) {
        return java.util.Optional.ofNullable(mapper.findForReplay(id));
    }

    /**
     * 处理当前类型职责中的操作 {@code markReplayRequested}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     */
    @Override
    public void markReplayRequested(long id, long operatorId, String reason) {
        if (mapper.markReplayRequested(id, operatorId, reason) != 1) {
            throw new com.chaobo.scm.common.error.BusinessException(com.chaobo.scm.common.error.ErrorCode.STATE_CONFLICT, "只有已保存载荷的失败事件可重放");
        }
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
