package org.scm.bdp.service.application.command.product;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateProductCommand(
        @NotNull Long id,
        @NotBlank String name,
        String description,
        @NotNull Long categoryId,
        @NotNull Long unitId,
        String keyAttributes,
        String salesAttributes,

        BigDecimal referencePurchasePrice,
        BigDecimal referenceSalesPrice
){}
