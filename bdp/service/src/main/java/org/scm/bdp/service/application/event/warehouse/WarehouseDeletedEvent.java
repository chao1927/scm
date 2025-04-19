package org.scm.bdp.service.application.event.warehouse;

import org.scm.common.DomainEvent;

public record WarehouseDeletedEvent(Long warehouseId) implements DomainEvent {
    @Override
    public String topic() {
        return "warehouse-topic";
    }

    @Override
    public String type() {
        return "WarehouseDeleted";
    }
}