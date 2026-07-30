package com.chaobo.scm.supplier.infrastructure.persistence.asn;

import java.time.OffsetDateTime;

/**
 * AsnRow。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public record AsnRow(long asnId, String asnNo, long purchaseOrderId, long supplierId, long warehouseId, OffsetDateTime eta, OffsetDateTime shipAt, String carrierName, String trackingNo, int asnStatus, String cancelReason, int version) {
}
