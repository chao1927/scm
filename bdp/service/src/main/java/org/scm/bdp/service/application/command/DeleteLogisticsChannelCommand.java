package org.scm.bdp.service.application.command;

import jakarta.validation.constraints.NotNull;

public record DeleteLogisticsChannelCommand(
        @NotNull Long id
) {}