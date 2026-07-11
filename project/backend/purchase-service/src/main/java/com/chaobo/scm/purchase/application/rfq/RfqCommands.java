package com.chaobo.scm.purchase.application.rfq;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public final class RfqCommands {
    private RfqCommands() {
    }

    public record Create(
            int rfqType,
            long purchaseOrgId,
            String categoryCode,
            String sourceRequisitionNo,
            OffsetDateTime quoteDeadline,
            List<Line> lines,
            List<Long> invitedSupplierIds) {
    }

    public record Line(
            Long lineId,
            String skuCode,
            BigDecimal targetQty,
            String uom,
            LocalDate requiredDeliveryDate,
            String qualityRequirement) {
    }

    public record Version(int version) {
    }

    public record Close(int version, String reason) {
    }
}
