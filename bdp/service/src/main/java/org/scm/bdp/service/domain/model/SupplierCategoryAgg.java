package org.scm.bdp.service.domain.model;

import org.scm.bdp.service.adapter.infra.domain.SupplierCategory;
import org.scm.bdp.service.application.command.supplier.CreateSupplierCategoryCommand;
import org.scm.bdp.service.application.command.supplier.UpdateSupplierCategoryCommand;

public record SupplierCategoryAgg(SupplierCategory category) {

    public static SupplierCategoryAgg create(CreateSupplierCategoryCommand command) {
        // TODO 实现创建逻辑
        return null;
    }

    public Long id() {
        return category.getId();
    }

    public void update(UpdateSupplierCategoryCommand command) {
        // TODO 实现更新逻辑
    }

    public void delete() {

    }
}
