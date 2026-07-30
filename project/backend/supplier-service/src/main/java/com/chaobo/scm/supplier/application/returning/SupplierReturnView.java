package com.chaobo.scm.supplier.application.returning;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * SupplierReturnView。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public record SupplierReturnView(long id, String no, long supplierId, long warehouseId, Long qualityIssueId, String reason, int status, String statusLabel, String inventoryLockNo, OffsetDateTime supplierConfirmedAt, String outboundNo, String shipmentId, String waybillNo, String carrierCode, boolean settlementCompleted, String settlementRef, BigDecimal offsetAmount, BigDecimal claimAmount, String exceptionReason, int version, List<Line> lines) {

    /**
     * 处理当前类型职责中的操作 {@code Line}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @param batchNo 可追踪业务编码，类型为 {@code String}
     * @param inventoryStatus 生命周期状态，类型为 {@code String}
     * @param requestedQty 数量值，类型为 {@code BigDecimal}
     * @param lockedQty 数量值，类型为 {@code BigDecimal}
     * @param outboundQty 数量值，类型为 {@code BigDecimal}
     * @param signedQty 数量值，类型为 {@code BigDecimal}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code record}
     */
    public record Line(long id, String skuCode, String batchNo, String inventoryStatus, BigDecimal requestedQty, BigDecimal lockedQty, BigDecimal outboundQty, BigDecimal signedQty) {
    }
}
