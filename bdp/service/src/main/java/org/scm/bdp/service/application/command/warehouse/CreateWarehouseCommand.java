package org.scm.bdp.service.application.command.warehouse;

import jakarta.validation.constraints.NotBlank;

public record CreateWarehouseCommand(
        @NotBlank String name,
        String code,
        String address,
        String contactPerson,
        String phone
) {}