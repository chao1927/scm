package org.scm.bdp.service.application.command.warehouse;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateLocationCommand(
        @NotNull Long id,
        @NotNull Long warehouseId,
        @NotNull String code,

        @NotNull BigDecimal maxVolume,

        @NotNull BigDecimal maxWeight,

        @NotNull Integer mixingStrategy
) {}