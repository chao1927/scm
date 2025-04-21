package org.scm.bdp.service.application.command.supplier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateSupplierCommand(
        @NotNull Long id,
        @NotBlank String name,
        @NotNull Long categoryId,
        @NotBlank String contactPerson,
        @NotBlank String contactPhone,
        @NotBlank String address,
        @NotBlank String businessLicenseNumber,
        String businessLicensePhoto,
        @NotBlank String organizationCode
) {}