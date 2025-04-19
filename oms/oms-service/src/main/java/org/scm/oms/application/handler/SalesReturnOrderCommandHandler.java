package org.scm.oms.application.handler;

import org.scm.common.EventPublisher;
import org.scm.oms.application.command.CreateSalesReturnOrderCommand;
import org.scm.oms.application.command.UpdateReturnStatusCommand;
import org.scm.oms.application.event.SalesReturnOrderCreatedEvent;
import org.scm.oms.application.event.SalesReturnOrderStatusChangedEvent;
import org.scm.oms.domain.model.SalesReturnOrderAgg;
import org.scm.oms.domain.repository.SalesReturnOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SalesReturnOrderCommandHandler {

    @Autowired
    private SalesReturnOrderRepository repository;

    @Autowired
    private EventPublisher publisher;

    public void handle(CreateSalesReturnOrderCommand command) {
        SalesReturnOrderAgg agg = SalesReturnOrderAgg.create(command);
        repository.save(agg);
        publisher.publish(new SalesReturnOrderCreatedEvent(command.salesReturnNo()));
    }

    public void handle(UpdateReturnStatusCommand command) {
        SalesReturnOrderAgg agg = repository.findById(Long.valueOf(command.salesReturnNo()));
        agg.updateStatus(command.status());
        repository.save(agg);
        publisher.publish(new SalesReturnOrderStatusChangedEvent(command.salesReturnNo(), command.status()));
    }
}
