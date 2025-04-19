package org.scm.srm.wms.application.handler;

import org.scm.common.EventPublisher;
import org.scm.srm.wms.adapter.infra.domain.LocationInventory;
import org.scm.srm.wms.application.command.AdjustLocationInventoryCommand;
import org.scm.srm.wms.application.command.PlaceToLocationCommand;
import org.scm.srm.wms.application.event.LocationInventoryAdjustedEvent;
import org.scm.srm.wms.application.event.LocationInventoryPlacedEvent;
import org.scm.srm.wms.domain.model.LocationInventoryAgg;
import org.scm.srm.wms.domain.repository.LocationInventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LocationInventoryCommandHandler {

    @Autowired
    private LocationInventoryRepository repository;

    @Autowired
    private EventPublisher eventPublisher;

    public void handle(PlaceToLocationCommand command) {
        LocationInventoryAgg inventory = repository.findByLocationAndSku(command.locationId(), command.sku(), command.batchNo());
        // TODO 默认为可用库存

        repository.save(inventory);
        eventPublisher.publish(new LocationInventoryPlacedEvent(command.locationId(), command.sku(), command.quantity()));
    }

    public void handle(AdjustLocationInventoryCommand command) {
        LocationInventoryAgg inventory = repository.findById(command.id());
        inventory.setAvailableQuantity(command.newQuantity());
        repository.save(inventory);
        eventPublisher.publish(new LocationInventoryAdjustedEvent(command.id(), command.newQuantity()));
    }
}
