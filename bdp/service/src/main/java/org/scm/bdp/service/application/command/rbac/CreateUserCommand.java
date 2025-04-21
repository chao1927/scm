package org.scm.bdp.service.application.command.rbac;

import jakarta.validation.constraints.NotBlank;

public record CreateUserCommand(
    @NotBlank String username,
    @NotBlank String nickname,
    String email,
    @NotBlank String phone
) {}