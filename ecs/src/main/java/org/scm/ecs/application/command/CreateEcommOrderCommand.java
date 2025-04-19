package org.scm.ecs.application.command;

import java.math.BigDecimal;

public record CreateEcommOrderCommand(
        String platformOrderNo,
        String customerName,
        String customerPhone,
        String addressDetail,
        BigDecimal totalProductAmount,
        BigDecimal freight,
        BigDecimal actualPayment,
        String paymentMethod,
        String paymentTradeNo
) {}