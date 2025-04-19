package org.scm.bdp.service.application.command.product;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateProductCommand(
        @NotNull
        Long id,
        @NotBlank
        String name,
        @NotNull
        Long categoryId,
        @NotNull
        Long unitId,
        String description,
        BigDecimal purchasePrice,
        BigDecimal salePrice
){}
