package org.scm.srm.wms.application.handler;

import org.scm.common.EventPublisher;
import org.scm.srm.wms.application.command.InspectStorageCommand;
import org.scm.srm.wms.application.command.ShelfStorageCommand;
import org.scm.srm.wms.application.command.StartStorageCommand;
import org.scm.srm.wms.application.event.StorageInspectedEvent;
import org.scm.srm.wms.application.event.StorageShelvedEvent;
import org.scm.srm.wms.application.event.StorageStartedEvent;
import org.scm.srm.wms.domain.model.PurchaseStorageAgg;
import org.scm.srm.wms.domain.repository.PurchaseStorageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PurchaseStorageCommandHandler {

    @Autowired
    private PurchaseStorageRepository repository;
    @Autowired
    private EventPublisher eventPublisher;

    public void handle(StartStorageCommand command) {
        PurchaseStorageAgg agg = repository.findByStorageNo(command.storageNo());
        agg.start(command.operatorEmpId());
        repository.save(agg);
        eventPublisher.publish(new StorageStartedEvent(command.storageNo()));
    }

    public void handle(InspectStorageCommand command) {
        PurchaseStorageAgg agg = repository.findByStorageNo(command.storageNo());
        agg.inspect();
        repository.save(agg);
        eventPublisher.publish(new StorageInspectedEvent(command.storageNo()));
    }

    public void handle(ShelfStorageCommand command) {
        PurchaseStorageAgg agg = repository.findByStorageNo(command.storageNo());
        agg.shelf();
        repository.save(agg);
        eventPublisher.publish(new StorageShelvedEvent(command.storageNo()));
    }
}
