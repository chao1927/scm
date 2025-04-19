package org.scm.bdp.service.application.handler;

import org.scm.bdp.service.application.command.warehouse.*;
import org.scm.bdp.service.application.event.warehouse.*;
import org.scm.bdp.service.domain.model.WarehouseAgg;
import org.scm.bdp.service.domain.repository.WarehouseRepository;
import org.scm.common.EventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WarehouseLocationCommandHandler {

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private EventPublisher eventPublisher;

    public void handle(AddLocationToWarehouseCommand command) {
        WarehouseAgg agg = warehouseRepository.findById(command.warehouseId());
        agg.addLocation(command);
        warehouseRepository.save(agg);
        eventPublisher.publish(new LocationAddedEvent(
                command.warehouseId(),
                agg.getLastAddedLocationId(),
                command.locationCode()
        ));
    }

    public void handle(UpdateLocationCommand command) {
        WarehouseAgg agg = warehouseRepository.findById(command.warehouseId());
        agg.updateLocation(command);
        warehouseRepository.save(agg);
        eventPublisher.publish(new LocationUpdatedEvent(
                command.warehouseId(),
                command.locationId(),
                command.locationCode()
        ));
    }

    public void handle(DisableLocationCommand command) {
        WarehouseAgg agg = warehouseRepository.findById(command.warehouseId());
        agg.disableLocation(command.locationId());
        warehouseRepository.save(agg);
        eventPublisher.publish(new LocationDisabledEvent(command.warehouseId(), command.locationId()));
    }

    public void handle(EnableLocationCommand command) {
        WarehouseAgg agg = warehouseRepository.findById(command.warehouseId());
        agg.enableLocation(command.locationId());
        warehouseRepository.save(agg);
        eventPublisher.publish(new LocationEnabledEvent(command.warehouseId(), command.locationId()));
    }

    public void handle(DeleteLocationCommand command) {
        WarehouseAgg agg = warehouseRepository.findById(command.warehouseId());
        agg.deleteLocation(command.locationId());
        warehouseRepository.save(agg);
        eventPublisher.publish(new LocationDeletedEvent(command.warehouseId(), command.locationId()));
    }
}
