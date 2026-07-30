package com.chaobo.scm.supplier.application.masterdata;

/**
 * MasterDataEventConsumeLogPort。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。定义跨进程或跨层协作端口，隔离调用方与具体技术实现。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public interface MasterDataEventConsumeLogPort {

    /**
     * ClaimResult。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。定义封闭的业务状态或类别集合，避免使用含义不明的数字和字符串。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    enum ClaimResult {

        // 业务枚举值：claimed
        CLAIMED,
        // 业务枚举值：already succeeded
        ALREADY_SUCCEEDED,
        // 业务枚举值：in progress
        IN_PROGRESS
    }

    /**
     * 处理当前类型职责中的操作 {@code claim}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @param eventType 业务处理参数或成员，类型为 {@code String}
     * @param consumerName 业务处理参数或成员，类型为 {@code String}
     * @param idempotentKey 业务或技术标识，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ClaimResult}
     */
    ClaimResult claim(String sourceSystem, String eventCode, String eventType, String consumerName, String idempotentKey);

    /**
     * 处理当前类型职责中的操作 {@code markSucceeded}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @param consumerName 业务处理参数或成员，类型为 {@code String}
     * @param ignored 业务处理参数或成员，类型为 {@code boolean}
     */
    void markSucceeded(String sourceSystem, String eventCode, String consumerName, boolean ignored);

    /**
     * 处理当前类型职责中的操作 {@code recordFailure}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @param eventType 业务处理参数或成员，类型为 {@code String}
     * @param consumerName 业务处理参数或成员，类型为 {@code String}
     * @param idempotentKey 业务或技术标识，类型为 {@code String}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     */
    void recordFailure(String sourceSystem, String eventCode, String eventType, String consumerName, String idempotentKey, String reason);

    /**
     * 执行命令 {@code savePayload}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @param consumerName 业务处理参数或成员，类型为 {@code String}
     * @param payloadJson 业务处理参数或成员，类型为 {@code String}
     */
    default void savePayload(String sourceSystem, String eventCode, String consumerName, String payloadJson) {
    }

    /**
     * 查询并返回 {@code findForReplay}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param consumeLogId 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code java.util.Optional<ReplayEvent>}
     */
    default java.util.Optional<ReplayEvent> findForReplay(long consumeLogId) {
        return java.util.Optional.empty();
    }

    /**
     * 处理当前类型职责中的操作 {@code markReplayRequested}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param consumeLogId 业务或技术标识，类型为 {@code long}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     */
    default void markReplayRequested(long consumeLogId, long operatorId, String reason) {
    }

    /**
     * ReplayEvent。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。表达已经发生的业务事实，载荷用于跨事务或跨上下文可靠传播。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record ReplayEvent(long id, String sourceSystem, String eventCode, String eventType, String consumerName, String payloadJson, int status) {
    }
}
