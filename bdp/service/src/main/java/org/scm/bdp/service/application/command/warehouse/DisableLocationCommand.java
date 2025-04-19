package org.scm.bdp.service.application.command.warehouse;

import jakarta.validation.constraints.NotNull;

public record DisableLocationCommand(
        @NotNull Long warehouseId,
        @NotNull Long locationId
) {}