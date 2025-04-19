package org.scm.bdp.service.adapter.controller;

import org.scm.bdp.service.application.command.warehouse.*;
import org.scm.bdp.service.application.event.warehouse.*;
import org.scm.bdp.service.domain.model.WarehouseAgg;
import org.scm.bdp.service.domain.repository.WarehouseRepository;
import org.scm.common.EventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WarehouseCommandHandler {

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private EventPublisher eventPublisher;

    public void handle(CreateWarehouseCommand command) {
        WarehouseAgg agg = WarehouseAgg.create(command);
        warehouseRepository.save(agg);
        eventPublisher.publish(new WarehouseCreatedEvent(
                agg.warehouse().getId(),
                agg.warehouse().getName()
        ));
    }

    public void handle(UpdateWarehouseCommand command) {
        WarehouseAgg agg = warehouseRepository.findById(command.id());
        agg.update(command);
        warehouseRepository.save(agg);
        eventPublisher.publish(new WarehouseUpdatedEvent(
                agg.warehouse().getId(),
                agg.warehouse().getName()
        ));
    }

    public void handle(DisableWarehouseCommand command) {
        WarehouseAgg agg = warehouseRepository.findById(command.id());
        agg.disable();
        warehouseRepository.save(agg);
        eventPublisher.publish(new WarehouseDisabledEvent(agg.warehouse().getId()));
    }

    public void handle(EnableWarehouseCommand command) {
        WarehouseAgg agg = warehouseRepository.findById(command.id());
        agg.enable();
        warehouseRepository.save(agg);
        eventPublisher.publish(new WarehouseEnabledEvent(agg.warehouse().getId()));
    }

    public void handle(DeleteWarehouseCommand command) {
        WarehouseAgg agg = warehouseRepository.findById(command.id());
        agg.delete();
        warehouseRepository.save(agg);
        eventPublisher.publish(new WarehouseDeletedEvent(agg.warehouse().getId()));
    }
}
