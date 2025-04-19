package org.scm.srm.wms.application.handler;

import org.scm.common.EventPublisher;
import org.scm.srm.wms.application.command.*;
import org.scm.srm.wms.application.event.*;
import org.scm.srm.wms.domain.model.WarehouseInventoryAgg;
import org.scm.srm.wms.domain.repository.WarehouseInventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WarehouseInventoryCommandHandler {

    @Autowired
    private WarehouseInventoryRepository repository;
    @Autowired
    private EventPublisher eventPublisher;

    public void handle(IncreaseInventoryCommand command) {
        WarehouseInventoryAgg agg = repository.findByWarehouseIdAndSku(command.warehouseId(), command.sku());
        agg.increaseInventory(command.quantity());
        repository.save(agg);
        eventPublisher.publish(new WarehouseInventoryIncreasedEvent(command.warehouseId(), command.sku(), command.quantity()));
    }

    public void handle(LockInventoryCommand command) {
        WarehouseInventoryAgg agg = repository.findByWarehouseIdAndSku(command.warehouseId(), command.sku());
        agg.lockInventory(command.quantity());
        repository.save(agg);
        eventPublisher.publish(new WarehouseInventoryLockedEvent(command.warehouseId(), command.sku(), command.quantity()));
    }

    public void handle(UnlockInventoryCommand command) {
        var agg = repository.findByWarehouseIdAndSku(command.warehouseId(), command.sku());
        agg.unlockInventory(command.quantity());
        repository.save(agg);
        eventPublisher.publish(new WarehouseInventoryUnlockedEvent(command.warehouseId(), command.sku(), command.quantity()));
    }

    public void handle(FreezeInventoryCommand command) {
        var agg = repository.findByWarehouseIdAndSku(command.warehouseId(), command.sku());
        agg.freezeInventory(command.quantity());
        repository.save(agg);
        eventPublisher.publish(new WarehouseInventoryFrozenEvent(command.warehouseId(), command.sku(), command.quantity()));
    }

    public void handle(UnfreezeInventoryCommand command) {
        var agg = repository.findByWarehouseIdAndSku(command.warehouseId(), command.sku());
        agg.unfreezeInventory(command.quantity());
        repository.save(agg);
        eventPublisher.publish(new WarehouseInventoryUnfrozenEvent(command.warehouseId(), command.sku(), command.quantity()));
    }
}
