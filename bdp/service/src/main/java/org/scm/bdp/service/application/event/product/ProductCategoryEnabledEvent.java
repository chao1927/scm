package org.scm.bdp.service.application.event.product;

import org.scm.common.DomainEvent;

public record ProductCategoryEnabledEvent(Long id) implements DomainEvent {
    @Override
    public String topic() {
        return null;
    }

    @Override
    public String type() {
        return null;
    }
}
