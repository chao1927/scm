package org.scm.oms.application.command;

import java.math.BigDecimal;

public record CreateSalesReturnOrderCommand(
        String salesReturnNo,
        String orderNo,
        String returnApplyNo,
        String refundReason,
        BigDecimal refundAmount,
        Integer returnType,
        String logisticsNo
) {}