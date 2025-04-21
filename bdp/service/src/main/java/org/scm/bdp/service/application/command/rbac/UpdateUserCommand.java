package org.scm.bdp.service.application.command.rbac;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateUserCommand(
        @NotNull Long id,
        @NotBlank String username,
        @NotBlank String nickname,
        String email,
        @NotBlank String phone) {
}
