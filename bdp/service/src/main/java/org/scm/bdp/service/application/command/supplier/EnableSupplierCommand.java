package org.scm.bdp.service.application.command.supplier;

import jakarta.validation.constraints.NotNull;

public record EnableSupplierCommand(
        @NotNull Long id
) {}