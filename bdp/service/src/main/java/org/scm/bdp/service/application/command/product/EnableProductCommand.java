package org.scm.bdp.service.application.command.product;

import jakarta.validation.constraints.NotNull;

public record EnableProductCommand(
        @NotNull
        Long id
){}
