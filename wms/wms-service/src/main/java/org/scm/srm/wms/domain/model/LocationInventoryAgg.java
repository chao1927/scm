package org.scm.srm.wms.domain.model;

import org.scm.srm.wms.adapter.infra.domain.LocationInventory;
import org.scm.srm.wms.application.command.AdjustLocationInventoryCommand;
import org.scm.srm.wms.application.command.PlaceToLocationCommand;

public record LocationInventoryAgg(LocationInventory inventory) {

    public void placeToLocation(PlaceToLocationCommand command) {
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + command.quantity());
    }

    public void adjustInventory(AdjustLocationInventoryCommand command) {
        inventory.setAvailableQuantity(command.newQuantity());
    }

    public void setAvailableQuantity(Integer newQuantity) {

    }
}
