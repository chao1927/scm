package com.chaobo.scm.supplier.domain.shared;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * DomainEvent。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。表达已经发生的业务事实，载荷用于跨事务或跨上下文可靠传播。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public record DomainEvent(long eventId, String eventCode, String eventType, String eventName, String aggregateType, long aggregateId, String aggregateNo, int aggregateVersion, long operatorId, OffsetDateTime occurredAt, Map<String, Object> payload) {

    public DomainEvent {
        payload = Map.copyOf(payload);
    }
}
