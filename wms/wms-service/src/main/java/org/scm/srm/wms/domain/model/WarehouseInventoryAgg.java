package org.scm.srm.wms.domain.model;

import org.scm.common.exception.BizException;
import org.scm.srm.wms._share.enums.InventoryErrorCode;
import org.scm.srm.wms.adapter.infra.domain.WarehouseInventory;

public record WarehouseInventoryAgg(WarehouseInventory inventory) {


    public void increaseInventory(int quantity) {
        inventory.setTotalQuantity(inventory.getTotalQuantity() + quantity);
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
    }

    public void lockInventory(int quantity) {
        if (inventory.getAvailableQuantity() < quantity) {
            throw new BizException(InventoryErrorCode.INSUFFICIENT_INVENTORY);
        }
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
        inventory.setLockedQuantity(inventory.getLockedQuantity() + quantity);
    }

    public void unlockInventory(int quantity) {
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
        inventory.setLockedQuantity(inventory.getLockedQuantity() - quantity);
    }

    public void freezeInventory(int quantity) {
        if (inventory.getAvailableQuantity() < quantity) {
            throw new BizException(InventoryErrorCode.INSUFFICIENT_INVENTORY);
        }
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
        inventory.setInTransitQuantity(inventory.getInTransitQuantity() + quantity);
    }

    public WarehouseInventory inventory() {
        return inventory;
    }

    public void unfreezeInventory(Integer quantity) {

    }
}
