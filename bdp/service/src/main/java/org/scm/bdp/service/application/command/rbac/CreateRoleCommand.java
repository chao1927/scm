package org.scm.bdp.service.application.command.rbac;

import jakarta.validation.constraints.NotBlank;

public record CreateRoleCommand(
        @NotBlank String name,
        @NotBlank String code,
        String remark
) {
}
