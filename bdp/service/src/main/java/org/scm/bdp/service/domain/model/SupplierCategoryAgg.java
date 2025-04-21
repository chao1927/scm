package org.scm.bdp.service.domain.model;

import org.scm.bdp.service.adapter.infra.domain.SupplierCategory;

public record SupplierCategoryAgg(SupplierCategory category) {

    public Long id() {
        return category.getId();
    }

    public void update(String name, String description) {
        category.setName(name);
        category.setDescription(description);
    }

}
