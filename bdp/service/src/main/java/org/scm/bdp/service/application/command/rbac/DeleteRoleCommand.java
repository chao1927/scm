package org.scm.bdp.service.application.command.rbac;

import jakarta.validation.constraints.NotNull;

public record DeleteRoleCommand(
        @NotNull Long id
) {
}
