package org.scm.bdp.service.application.command.rbac;

import jakarta.validation.constraints.NotBlank;

public record LoginCommand(
        @NotBlank String username,
        @NotBlank String password
) {
}
