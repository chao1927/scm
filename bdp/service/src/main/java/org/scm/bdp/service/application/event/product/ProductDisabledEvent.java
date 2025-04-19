package org.scm.bdp.service.application.event.product;


import org.scm.common.DomainEvent;

public record ProductDisabledEvent (Long productId) implements DomainEvent {
    @Override
    public String topic() {
        return "product-topic";
    }

    @Override
    public String type() {
        return "ProductDisabled";
    }
}
