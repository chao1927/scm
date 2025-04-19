package org.scm.bdp.service.application.command.product;

import jakarta.validation.constraints.NotNull;

public record DeleteProductCommand(
        @NotNull
        Long id
){}
