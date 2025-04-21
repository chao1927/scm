package org.scm.bdp.service.application.command.warehouse;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateWarehouseCommand(
        @NotNull Long id,
        @NotBlank String name,
        String description,
        @NotBlank String address,
        @NotNull Integer type,
        BigDecimal area,

        @NotBlank String manager,

        @NotBlank String managerPhone
) {}