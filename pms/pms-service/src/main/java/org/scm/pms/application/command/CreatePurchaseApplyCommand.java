package org.scm.pms.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreatePurchaseApplyCommand(
        @NotBlank String applyNo,
        @NotNull Long applyEmpId,
        Long purchaserEmpId,
        BigDecimal estimatedTotalPrice
) {
}