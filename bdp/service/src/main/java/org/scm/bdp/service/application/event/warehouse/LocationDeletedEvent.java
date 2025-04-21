package org.scm.bdp.service.application.event.warehouse;

import org.scm.common.DomainEvent;

public record LocationDeletedEvent(Long locationId) implements DomainEvent {
    @Override
    public String topic() {
        return "warehouse-location-topic";
    }

    @Override
    public String type() {
        return "LocationDeleted";
    }
}