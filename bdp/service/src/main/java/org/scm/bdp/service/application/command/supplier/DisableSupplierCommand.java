package org.scm.bdp.service.application.command.supplier;

import jakarta.validation.constraints.NotNull;

public record DisableSupplierCommand(
        @NotNull Long id
) {}