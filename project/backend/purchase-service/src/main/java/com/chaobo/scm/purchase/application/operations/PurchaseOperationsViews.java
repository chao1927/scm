package com.chaobo.scm.purchase.application.operations;

import java.time.OffsetDateTime;
import java.math.BigDecimal;

/**
 * PurchaseOperationsViews。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public final class PurchaseOperationsViews {

    /**
     * 创建 PurchaseOperationsViews。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     */
    private PurchaseOperationsViews() {
    }

    /**
     * FailedEvent。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。表达已经发生的业务事实，载荷用于跨事务或跨上下文可靠传播。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record FailedEvent(long id, String sourceSystem, String eventCode, String eventType, String consumerName, int retryCount, String reason, OffsetDateTime updatedAt) {
    }

    /**
     * FailedCommand。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record FailedCommand(long commandId, String commandType, String targetSystem, String businessNo, int retryCount, String reason, OffsetDateTime updatedAt) {
    }

    /** 供应商报价事件在采购上下文中的事实投影。 */
    public record Quotation(long factId, String eventCode, String quoteNo, String rfqNo,
                            long supplierId, String skuCode, BigDecimal quoteQty,
                            BigDecimal quoteAmount, String currency, String quoteStatus,
                            OffsetDateTime updatedAt) {
    }

    /** 采购业务操作日志。 */
    public record OperationLog(long id, String requestId, String traceId, long operatorId,
                               String operatorName, String operation, String targetType,
                               long targetId, String targetNo, OffsetDateTime createdAt) {
    }
}
