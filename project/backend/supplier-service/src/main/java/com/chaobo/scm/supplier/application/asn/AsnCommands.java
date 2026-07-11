package com.chaobo.scm.supplier.application.asn;

import com.chaobo.scm.supplier.domain.asn.AsnAggregate;
import com.chaobo.scm.supplier.domain.asn.ShipmentInfo;

import java.time.OffsetDateTime;
import java.util.List;

public final class AsnCommands {
    private AsnCommands() {}

    public record Create(
            long purchaseOrderId,
            long supplierId,
            long warehouseId,
            OffsetDateTime estimatedArrivalAt,
            List<AsnAggregate.NewLine> lines) {}

    public record Submit(long asnId, int version) {}

    public record Cancel(long asnId, String reason, int version) {}

    public record ConfirmShipment(long asnId, ShipmentInfo shipmentInfo, int version) {}
}
