package org.scm.bdp.service.application.event.product;

import org.scm.common.DomainEvent;

public record ProductUpdatedEvent(
        Long productId,
        String name
) implements DomainEvent {

    @Override
    public String topic() {
        // TODO: specify topic
        return "product-topic";
    }

    @Override
    public String type() {
        return "ProductUpdated";
    }
}
