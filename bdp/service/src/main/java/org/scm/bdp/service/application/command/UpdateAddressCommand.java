package org.scm.bdp.service.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateAddressCommand(
        @NotNull Long id,
        @NotBlank String province,
        @NotBlank String city,

        @NotBlank String district,
        @NotBlank String detailedAddress
) {
}
