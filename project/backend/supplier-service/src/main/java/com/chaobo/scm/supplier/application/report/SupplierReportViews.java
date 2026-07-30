package com.chaobo.scm.supplier.application.report;

import java.math.BigDecimal;

/**
 * SupplierReportViews。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public final class SupplierReportViews {

    /**
     * 创建 SupplierReportViews。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     */
    private SupplierReportViews() {
    }

    /**
     * Fulfillment。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Fulfillment(long supplierId, long purchaseOrders, long confirmedOrders, long pendingOrders, long asnCount, long shippedAsns, long receivedAsns, BigDecimal plannedQty, BigDecimal receivedQty, BigDecimal receiveRate) {
    }

    /**
     * ExceptionOverview。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ExceptionOverview(long supplierId, long openQualityIssues, long overdueQualityIssues, long openReturns, long transportExceptions, long openWarnings, long failedInboundEvents, long failedOutboundEvents) {
    }
}
