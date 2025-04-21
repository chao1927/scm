package org.scm.bdp.service.application.event.product;

import org.scm.common.DomainEvent;

public record ProductCategoryUpdatedEvent(Long id) implements DomainEvent {
    @Override
    public String topic() {
        return ProductCategoryUpdatedEvent.class.getSimpleName();
    }

    @Override
    public String type() {
        return null;
    }
}