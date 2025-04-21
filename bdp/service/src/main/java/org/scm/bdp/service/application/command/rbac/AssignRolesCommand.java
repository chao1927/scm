package org.scm.bdp.service.application.command.rbac;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AssignRolesCommand(
        @NotNull Long userId,
        List<Long> roleIds) {}
