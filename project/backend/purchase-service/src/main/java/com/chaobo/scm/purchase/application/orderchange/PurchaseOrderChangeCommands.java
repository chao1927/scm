package com.chaobo.scm.purchase.application.orderchange;

import java.math.BigDecimal;
import java.util.Map;

public final class PurchaseOrderChangeCommands {
    private PurchaseOrderChangeCommands() {
    }

    public record Create(String orderNo, int changeType, String beforeSnapshot, String afterSnapshot,
                         String changeReason, Map<Long, BigDecimal> lineQtyChanges) {
    }

    public record Approve(int version, boolean approved) {
    }
}
