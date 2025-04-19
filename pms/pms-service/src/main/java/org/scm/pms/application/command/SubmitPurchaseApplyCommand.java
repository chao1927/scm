package org.scm.pms.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SubmitPurchaseApplyCommand(
    @NotBlank String applyNo,
    @NotNull Long applyEmpId,
    @NotNull Long purchaserEmpId,
    @NotNull BigDecimal estimatedTotalPrice
) {}