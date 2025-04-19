package org.scm.bdp.service.application.command.product;

public record CreateProductCategoryCommand(
        Long id,
        String name,
        Long parentId,
        String attributes,
        Integer sortOrder
) {}