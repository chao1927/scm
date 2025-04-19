package org.scm.bdp.service.application.command.supplier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateSupplierCommand(
        @NotBlank String name,
        @NotNull Long categoryId,
        String contactPerson,
        String phone,
        String address
) {}