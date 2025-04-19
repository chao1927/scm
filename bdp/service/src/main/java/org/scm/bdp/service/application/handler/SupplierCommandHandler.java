package org.scm.bdp.service.application.handler;

import org.scm.bdp.service.application.command.supplier.*;
import org.scm.bdp.service.application.event.supplier.*;
import org.scm.bdp.service.domain.model.SupplierAgg;
import org.scm.bdp.service.domain.repository.SupplierRepository;
import org.scm.common.EventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SupplierCommandHandler {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private EventPublisher eventPublisher;

    public void handle(CreateSupplierCommand command) {
        SupplierAgg agg = SupplierAgg.create(command);
        supplierRepository.save(agg);
        eventPublisher.publish(new SupplierCreatedEvent(
                agg.supplier().getId(),
                agg.supplier().getName()
        ));
    }

    public void handle(UpdateSupplierCommand command) {
        SupplierAgg agg = supplierRepository.findById(command.id());
        agg.update(command);
        supplierRepository.save(agg);
        eventPublisher.publish(new SupplierUpdatedEvent(
                agg.supplier().getId(),
                agg.supplier().getName()
        ));
    }

    public void handle(DisableSupplierCommand command) {
        SupplierAgg agg = supplierRepository.findById(command.id());
        agg.disable();
        supplierRepository.save(agg);
        eventPublisher.publish(new SupplierDisabledEvent(agg.supplier().getId()));
    }

    public void handle(EnableSupplierCommand command) {
        SupplierAgg agg = supplierRepository.findById(command.id());
        agg.enable();
        supplierRepository.save(agg);
        eventPublisher.publish(new SupplierEnabledEvent(agg.supplier().getId()));
    }

    public void handle(DeleteSupplierCommand command) {
        SupplierAgg agg = supplierRepository.findById(command.id());
        agg.delete();
        supplierRepository.save(agg);
        eventPublisher.publish(new SupplierDeletedEvent(agg.supplier().getId()));
    }
}
