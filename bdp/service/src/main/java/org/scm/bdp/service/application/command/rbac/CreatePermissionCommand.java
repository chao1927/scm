package org.scm.bdp.service.application.command.rbac;

import jakarta.validation.constraints.NotBlank;

public record CreatePermissionCommand(
        @NotBlank String name,
        @NotBlank String code,
        @NotBlank String path,
        @NotBlank String method,
        String description,
        @NotBlank Long parentId
        ) {
}
