package org.scm.bdp.service.application.event.product;

import org.scm.common.DomainEvent;

public record ProductCategoryUpdatedEvent(
        Long id,
        String name
) implements DomainEvent {
    @Override
    public String topic() {
        // TODO: 实现topic
        return null;
    }

    @Override
    public String type() {
        // todo 实现type
        return null;
    }
}