package org.scm.bdp.service.application.handler;

import org.scm.bdp.service.application.command.supplier.CreateSupplierCategoryCommand;
import org.scm.bdp.service.application.command.supplier.DeleteSupplierCategoryCommand;
import org.scm.bdp.service.application.command.supplier.UpdateSupplierCategoryCommand;
import org.scm.bdp.service.application.event.supplier.SupplierCategoryCreatedEvent;
import org.scm.bdp.service.application.event.supplier.SupplierCategoryDeletedEvent;
import org.scm.bdp.service.application.event.supplier.SupplierCategoryUpdatedEvent;
import org.scm.bdp.service.domain.model.SupplierCategoryAgg;
import org.scm.bdp.service.domain.repository.SupplierCategoryRepository;
import org.scm.common.EventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SupplierCategoryCommandHandler {

    @Autowired
    private SupplierCategoryRepository supplierCategoryRepository;

    @Autowired
    private EventPublisher eventPublisher;

    public void handle(CreateSupplierCategoryCommand command) {
        SupplierCategoryAgg agg = SupplierCategoryAgg.create(command);
        supplierCategoryRepository.save(agg);
        eventPublisher.publish(new SupplierCategoryCreatedEvent(
                agg.category().getId(),
                agg.category().getName()
        ));
    }

    public void handle(UpdateSupplierCategoryCommand command) {
        SupplierCategoryAgg agg = supplierCategoryRepository.findById(command.id());
        agg.update(command);
        supplierCategoryRepository.save(agg);
        eventPublisher.publish(new SupplierCategoryUpdatedEvent(
                agg.category().getId(),
                agg.category().getName()
        ));
    }

    public void handle(DeleteSupplierCategoryCommand command) {
        SupplierCategoryAgg agg = supplierCategoryRepository.findById(command.id());
        agg.delete();
        supplierCategoryRepository.save(agg);
        eventPublisher.publish(new SupplierCategoryDeletedEvent(agg.category().getId()));
    }
}
