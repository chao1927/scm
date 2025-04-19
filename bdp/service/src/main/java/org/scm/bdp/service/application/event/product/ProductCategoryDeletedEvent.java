package org.scm.bdp.service.application.event.product;

import org.scm.common.DomainEvent;

public record ProductCategoryDeletedEvent(Long id) implements DomainEvent {
    @Override
    public String topic() {
        // todo 实现topic
        return null;
    }

    @Override
    public String type() {
        // todo 实现type
        return null;
    }
}
