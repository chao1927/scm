package org.scm.bdp.service.application.command.rbac;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdatePermissionCommand(
        @NotNull Long id,
        @NotBlank String name,
        @NotBlank String code,

        @NotBlank String path,

        @NotBlank String method,

        String description,

        @NotBlank Long parentId
) {}
