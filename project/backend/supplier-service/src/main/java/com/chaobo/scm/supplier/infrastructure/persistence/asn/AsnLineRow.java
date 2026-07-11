package com.chaobo.scm.supplier.infrastructure.persistence.asn;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AsnLineRow(
        long asnLineId,
        long asnId,
        String skuCode,
        BigDecimal plannedQty,
        BigDecimal receivedQty,
        String batchNo,
        LocalDate productionDate,
        LocalDate expireDate) {}
