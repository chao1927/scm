package org.scm.bdp.service.application.command.warehouse;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateWarehouseCommand(
        @NotNull Long id,
        @NotBlank String name,
        String code,
        String address,
        String contactPerson,
        String phone
) {}