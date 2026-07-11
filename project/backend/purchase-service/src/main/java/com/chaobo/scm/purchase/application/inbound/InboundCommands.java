package com.chaobo.scm.purchase.application.inbound;

import java.math.BigDecimal;

public final class InboundCommands {
    private InboundCommands() {
    }

    public record RecordAsn(String orderNo, String asnNo, long supplierId, long purchaseOrgId, String warehouseCode,
                            String skuCode, BigDecimal notifiedQty) {
    }

    public record SyncWms(int version, BigDecimal receivedQty, BigDecimal qualifiedQty, BigDecimal unqualifiedQty,
                          BigDecimal putawayQty, String reason) {
    }
}
