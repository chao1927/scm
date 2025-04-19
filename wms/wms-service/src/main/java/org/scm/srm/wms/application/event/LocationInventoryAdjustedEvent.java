package org.scm.srm.wms.application.event;

import org.scm.common.DomainEvent;

public record LocationInventoryAdjustedEvent(
        Long id,
        Integer newQuantity
) implements DomainEvent {
    @Override public String topic() { return "location-inventory-topic"; }
    @Override public String type() { return "LocationInventoryAdjusted"; }
}