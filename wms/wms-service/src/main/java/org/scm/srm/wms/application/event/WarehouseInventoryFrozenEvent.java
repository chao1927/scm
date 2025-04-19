package org.scm.srm.wms.application.event;

import org.scm.common.DomainEvent;

public record WarehouseInventoryFrozenEvent(Long warehouseId, String sku, Integer quantity) implements DomainEvent {
    @Override
    public String topic() { return "inventory-topic"; }

    @Override
    public String type() { return "WarehouseInventoryFrozen"; }
}