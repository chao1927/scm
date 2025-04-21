package org.scm.bdp.service.application.command.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateProductCategoryCommand(
        @NotNull Long id,
        @NotBlank String name,
        @NotNull Long parentId,
        String attributes,
        @NotNull Integer sortOrder
) {}