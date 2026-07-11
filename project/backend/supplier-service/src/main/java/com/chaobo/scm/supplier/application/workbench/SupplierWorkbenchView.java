package com.chaobo.scm.supplier.application.workbench;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record SupplierWorkbenchView(
        long pendingQuotes,
        long pendingPurchaseOrderConfirms,
        long pendingAsns,
        long pendingReconciliations,
        long pendingRectifications,
        long openWarnings,
        long failedEvents,
        long openReturns,
        BigDecimal latestScore,
        OffsetDateTime generatedAt,
        List<TodoGroup> todoGroups,
        List<WarningGroup> warningGroups) {

    public record TodoGroup(String type, long count) {
    }

    public record WarningGroup(String level, long count) {
    }
}
