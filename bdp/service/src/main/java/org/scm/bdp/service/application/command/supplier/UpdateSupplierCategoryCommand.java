package org.scm.bdp.service.application.command.supplier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateSupplierCategoryCommand(
        @NotNull Long id,
        @NotBlank String name,
        String description
) {}