package org.scm.bdp.service.application.command.supplier;

import jakarta.validation.constraints.NotNull;

public record DeleteSupplierCommand(
        @NotNull Long id
) {}