package com.chaobo.scm.oms.domain;

import java.time.LocalDateTime;

/**
 * OmsEvent。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。表达已经发生的业务事实，载荷用于跨事务或跨上下文可靠传播。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public record OmsEvent(String eventType, String businessNo, String payload, LocalDateTime occurredAt) {

    /**
     * 处理当前类型职责中的操作 {@code of}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param eventType 业务处理参数或成员，类型为 {@code String}
     * @param businessNo 可追踪业务编码，类型为 {@code String}
     * @param payload 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code OmsEvent}
     */
    public static OmsEvent of(String eventType, String businessNo, String payload) {
        return new OmsEvent(eventType, businessNo, payload, LocalDateTime.now());
    }
}
