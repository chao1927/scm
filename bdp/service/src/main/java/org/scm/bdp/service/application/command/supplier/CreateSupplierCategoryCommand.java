package org.scm.bdp.service.application.command.supplier;

import jakarta.validation.constraints.NotBlank;

public record CreateSupplierCategoryCommand(
        @NotBlank String name,
        String description
) {}