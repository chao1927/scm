package org.scm.oms.application.command;

import java.math.BigDecimal;

public record SubmitReturnApplyCommand(
        String returnApplyNo,
        String orderNo,
        Integer applyType,
        BigDecimal refundAmount,
        String refundReason,
        Integer returnType
) {}