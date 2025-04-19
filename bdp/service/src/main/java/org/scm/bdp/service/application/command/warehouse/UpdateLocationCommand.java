package org.scm.bdp.service.application.command.warehouse;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateLocationCommand(
        @NotNull Long warehouseId,
        @NotNull Long locationId,
        @NotBlank String locationCode,
        @NotBlank String locationType,
        String description
) {}