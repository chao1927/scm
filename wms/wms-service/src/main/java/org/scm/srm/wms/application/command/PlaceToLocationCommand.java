package org.scm.srm.wms.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlaceToLocationCommand(
        @NotNull Long locationId,
        @NotBlank String sku,
        @NotBlank String batchNo,
        @NotNull Integer quantity
) {}