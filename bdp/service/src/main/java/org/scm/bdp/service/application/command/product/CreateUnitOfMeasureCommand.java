package org.scm.bdp.service.application.command.product;

import jakarta.validation.constraints.NotBlank;

public record CreateUnitOfMeasureCommand(
        @NotBlank String name
) {
}
