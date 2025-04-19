package org.scm.bdp.service.application.event.supplier;

import org.scm.common.DomainEvent;

public record SupplierCreatedEvent(
        Long supplierId,
        String name
) implements DomainEvent {

    @Override
    public String topic() {
        return "supplier-topic";
    }

    @Override
    public String type() {
        return "SupplierCreated";
    }
}