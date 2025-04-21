package org.scm.bdp.service.application.command;

import jakarta.validation.constraints.NotBlank;

public record CreateAddressCommand(
        @NotBlank String province,
        @NotBlank String city,

        @NotBlank String district,
        @NotBlank String detailedAddress
) {
}
