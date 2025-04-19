package org.scm.srm.wms.application.event;

import org.scm.common.DomainEvent;

public record LocationInventoryPlacedEvent(
        Long locationId,
        String sku,
        Integer quantity
) implements DomainEvent {
    @Override public String topic() { return "location-inventory-topic"; }
    @Override public String type() { return "LocationInventoryPlaced"; }
}