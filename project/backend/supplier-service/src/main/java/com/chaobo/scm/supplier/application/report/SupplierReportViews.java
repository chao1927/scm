package com.chaobo.scm.supplier.application.report;

import java.math.BigDecimal;

public final class SupplierReportViews {
    private SupplierReportViews() {
    }

    public record Fulfillment(long supplierId, long purchaseOrders, long confirmedOrders, long pendingOrders,
                              long asnCount, long shippedAsns, long receivedAsns, BigDecimal plannedQty,
                              BigDecimal receivedQty, BigDecimal receiveRate) {
    }

    public record ExceptionOverview(long supplierId, long openQualityIssues, long overdueQualityIssues,
                                    long openReturns, long transportExceptions, long openWarnings,
                                    long failedInboundEvents, long failedOutboundEvents) {
    }
}
