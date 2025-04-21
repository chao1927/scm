package org.scm.bdp.service.domain.model;

import org.scm.bdp.service._share.enums.SwitchStatus;
import org.scm.bdp.service.adapter.infra.domain.ProductCategory;

public record ProductCategoryAgg(ProductCategory category) {

    public Long id() {
        return category.getId();
    }

    public void update(String name, Long parentId, String attributes, Integer sortOrder) {
        category.setName(name);
        category.setParentId(parentId);
        category.setAttributes(attributes);
        category.setSortOrder(sortOrder);
    }

    public void enable() {
        category.setStatus(SwitchStatus.ENABLED.getValue());
    }

    public void disable() {
        category.setStatus(SwitchStatus.DISABLED.getValue());
    }

    public String name() {
        return category.getName();
    }
}
