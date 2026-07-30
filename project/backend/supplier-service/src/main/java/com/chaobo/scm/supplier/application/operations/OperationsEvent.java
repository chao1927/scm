package com.chaobo.scm.supplier.application.operations;

import java.time.OffsetDateTime;

/**
 * OperationsEvent。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。表达已经发生的业务事实，载荷用于跨事务或跨上下文可靠传播。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public record OperationsEvent(String sourceSystem, String eventCode, String eventType, long supplierId, String businessType, long businessId, String businessNo, String message, OffsetDateTime dueAt, OffsetDateTime occurredAt) {
}
