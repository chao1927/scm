package org.scm.bdp.service.application.command.product;

public record UpdateProductCategoryCommand(
        Long id,
        String name,
        Long parentId,
        String attributes,
        Integer sortOrder
) {}