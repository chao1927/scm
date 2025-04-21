package org.scm.bdp.service.application.command.rbac;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleCommand(
        @NotNull Long id,
        @NotBlank String name,
        @NotBlank  String code,
        String remark
) {
}
