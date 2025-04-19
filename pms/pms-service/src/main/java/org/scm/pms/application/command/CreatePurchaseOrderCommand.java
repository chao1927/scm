package org.scm.pms.application.command;

import java.math.BigDecimal;

public record CreatePurchaseOrderCommand(
    String applyNo,
    Long purchaserEmpId,
    Long supplierId,
    BigDecimal totalPrice
) {}