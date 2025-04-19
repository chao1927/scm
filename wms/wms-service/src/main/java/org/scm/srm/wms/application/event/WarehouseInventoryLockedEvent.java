package org.scm.srm.wms.application.event;

import org.scm.common.DomainEvent;

public record WarehouseInventoryLockedEvent(Long warehouseId, String sku, int quantity) implements DomainEvent {
    @Override public String topic() { return "inventory-topic"; }
    @Override public String type() { return "WarehouseInventoryLocked"; }
}