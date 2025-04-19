package org.scm.bdp.service.application.handler;

import org.scm.bdp.service.application.command.product.*;
import org.scm.bdp.service.application.event.product.*;
import org.scm.bdp.service.domain.model.ProductAgg;
import org.scm.bdp.service.domain.repository.ProductRepository;
import org.scm.common.EventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductCommandHandler {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EventPublisher eventPublisher;

    public void handle(CreateProductCommand command) {
        // TODO: command to aggregate
        ProductAgg product = null;
        productRepository.save(product);
        eventPublisher.publish(new ProductCreatedEvent(
                product.product().getId(),
                product.product().getName()
        ));
    }

    public void handle(UpdateProductCommand command) {
        ProductAgg product = productRepository.findById(command.id());
        product.update(command);
        productRepository.save(product);
        eventPublisher.publish(new ProductUpdatedEvent(
                product.product().getId(),
                product.product().getName()
        ));
    }

    public void handle(DisableProductCommand command) {
        ProductAgg product = productRepository.findById(command.id());
        product.disable();
        productRepository.save(product);
        eventPublisher.publish(new ProductDisabledEvent(product.product().getId()));
    }

    public void handle(EnableProductCommand command) {
        ProductAgg product = productRepository.findById(command.id());
        product.enable();
        productRepository.save(product);
        eventPublisher.publish(new ProductEnabledEvent(product.product().getId()));
    }

    public void handle(DeleteProductCommand command) {
        ProductAgg product = productRepository.findById(command.id());
        product.delete();
        productRepository.save(product);
        eventPublisher.publish(new ProductDeletedEvent(product.product().getId()));
    }
}
