package org.scm.bdp.service.application.command;

import jakarta.validation.constraints.NotBlank;

public record CreateLogisticsChannelCommand(
        @NotBlank String name,
        @NotBlank String code,
        String description
) {}