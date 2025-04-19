package org.scm.bdp.service.application.handler;

import lombok.extern.slf4j.Slf4j;
import org.scm.bdp.service.application.command.product.*;
import org.scm.bdp.service.application.event.product.ProductCategoryCreatedEvent;
import org.scm.bdp.service.application.event.product.ProductCategoryDeletedEvent;
import org.scm.bdp.service.application.event.product.ProductCategoryUpdatedEvent;
import org.scm.bdp.service.domain.model.ProductCategoryAgg;
import org.scm.bdp.service.domain.repository.ProductCategoryRepository;
import org.scm.common.EventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductCategoryCommandHandler {

    @Autowired
    private ProductCategoryRepository repository;

    @Autowired
    private EventPublisher eventPublisher;


    public void handle(CreateProductCategoryCommand cmd) {
        ProductCategoryAgg agg = ProductCategoryAgg.create(cmd);
        repository.save(agg);
        eventPublisher.publish(new ProductCategoryCreatedEvent(
                agg.category().getId(),
                agg.category().getName(),
                agg.category().getParentId()
        ));
    }

    public void handle(UpdateProductCategoryCommand cmd) {
        ProductCategoryAgg agg = repository.findById(cmd.id());
        agg.update(cmd);
        repository.save(agg);

        eventPublisher.publish(new ProductCategoryUpdatedEvent(
                agg.category().getId(),
                agg.category().getName()
        ));
    }

    public void handle(DisableProductCategoryCommand cmd) {
        ProductCategoryAgg agg = repository.findById(cmd.id());
        agg.disable();
        repository.save(agg);
    }


    public void handle(EnableProductCategoryCommand cmd) {
        ProductCategoryAgg agg = repository.findById(cmd.id());
        agg.enable();
        repository.save(agg);
    }

    public void handle(DeleteProductCategoryCommand cmd) {
        repository.deleteById(cmd.id());
        eventPublisher.publish(new ProductCategoryDeletedEvent(cmd.id()));
    }
}
