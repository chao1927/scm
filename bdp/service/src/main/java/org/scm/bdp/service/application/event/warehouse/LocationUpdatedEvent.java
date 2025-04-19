package org.scm.bdp.service.application.event.warehouse;

import org.scm.common.DomainEvent;

public record LocationUpdatedEvent(
        Long warehouseId,
        Long locationId,
        String locationCode
) implements DomainEvent {
    @Override
    public String topic() {
        return "warehouse-location-topic";
    }

    @Override
    public String type() {
        return "LocationUpdated";
    }
}