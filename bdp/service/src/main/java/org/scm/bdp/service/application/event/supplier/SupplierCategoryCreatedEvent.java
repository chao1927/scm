package org.scm.bdp.service.application.event.supplier;

import org.scm.common.DomainEvent;

public record SupplierCategoryCreatedEvent(
        Long supplierCategoryId
) implements DomainEvent {

    @Override
    public String topic() {
        return "supplier-category-topic";
    }

    @Override
    public String type() {
        return "SupplierCategoryCreated";
    }
}