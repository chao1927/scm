package org.scm.bdp.service.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateLogisticsChannelCommand(
        @NotNull Long id,
        @NotBlank String name,
        @NotBlank Integer serviceType,
        @NotBlank String coverageArea,

        @NotBlank String freightCalculationRules
) {}