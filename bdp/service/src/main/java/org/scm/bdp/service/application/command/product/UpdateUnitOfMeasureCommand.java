package org.scm.bdp.service.application.command.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateUnitOfMeasureCommand(
        @NotNull Long id,
        @NotBlank String name
) {
}
