package org.scm.bdp.service.application.command.rbac;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AssignPermissionsToRoleCommand(
        @NotNull Long roleId,
        List<Long> permissionIds
) {
}
