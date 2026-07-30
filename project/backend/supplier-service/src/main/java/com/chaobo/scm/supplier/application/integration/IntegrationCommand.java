package com.chaobo.scm.supplier.application.integration;

import java.time.OffsetDateTime;

/**
 * IntegrationCommand。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public record IntegrationCommand(long id, String code, String type, String aggregateType, long aggregateId, int aggregateVersion, String targetSystem, String payloadJson, int status, int retryCount, OffsetDateTime nextRetryAt, String remoteReference, String failReason) {
}
