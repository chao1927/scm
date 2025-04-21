package org.scm.bdp.service.application.event.product;


import org.scm.common.DomainEvent;

public record ProductCreatedEvent(
        Long productId
) implements DomainEvent {

    @Override
    public String topic() {
        return "product-topic";
    }

    @Override
    public String type() {
        return "ProductCreated";
    }
}
