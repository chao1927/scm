package com.chaobo.scm.purchase.application.rfq;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record RfqView(
        long id,
        String rfqNo,
        int rfqType,
        long purchaseOrgId,
        String categoryCode,
        String sourceRequisitionNo,
        OffsetDateTime quoteDeadline,
        int status,
        String statusName,
        OffsetDateTime publishedAt,
        String closeReason,
        int version,
        int invitedSupplierCount,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<Line> lines,
        List<Invitation> invitations) {

    public record Line(
            long lineId,
            String skuCode,
            BigDecimal targetQty,
            String uom,
            LocalDate requiredDeliveryDate,
            String qualityRequirement) {
    }

    public record Invitation(
            long invitationId,
            long supplierId,
            int quoteStatus) {
    }
}
