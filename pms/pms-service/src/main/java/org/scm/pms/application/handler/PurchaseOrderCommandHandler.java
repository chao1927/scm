package org.scm.pms.application.handler;

import org.scm.common.EventPublisher;
import org.scm.pms.application.command.*;
import org.scm.pms.application.event.*;
import org.scm.pms.domain.model.PurchaseOrderAgg;
import org.scm.pms.domain.repository.PurchaseOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PurchaseOrderCommandHandler {

    @Autowired
    private PurchaseOrderRepository repository;

    @Autowired
    private EventPublisher publisher;

    public void handle(CreatePurchaseOrderCommand command) {

        PurchaseOrderAgg agg = PurchaseOrderAgg.create(command);
        repository.save(agg);

        publisher.publish(new PurchaseOrderCreatedEvent(agg.id(), agg.purchaseOrder().getOrderNo()));
    }

    public void handle(SubmitPurchaseOrderCommand command) {
        PurchaseOrderAgg agg = repository.findById(command.id());
        agg.submit();
        repository.save(agg);
        publisher.publish(new PurchaseOrderSubmittedEvent(command.id()));
    }

    public void handle(AuditPurchaseOrderCommand command) {
        PurchaseOrderAgg agg = repository.findById(command.id());
        agg.audit();
        repository.save(agg);
        publisher.publish(new PurchaseOrderAuditedEvent(command.id()));
    }

    public void handle(ConfirmPurchaseOrderCommand command) {
        PurchaseOrderAgg agg = repository.findById(command.id());
        agg.confirm();
        repository.save(agg);
        publisher.publish(new PurchaseOrderConfirmedEvent(command.id()));
    }

    public void handle(ReceivePurchaseOrderCommand command) {
        PurchaseOrderAgg agg = repository.findById(command.id());
        agg.receive();
        repository.save(agg);
        publisher.publish(new PurchaseOrderReceivedEvent(command.id()));
    }

    public void handle(CompletePurchaseOrderCommand command) {
        PurchaseOrderAgg agg = repository.findById(command.id());
        agg.complete();
        repository.save(agg);
        publisher.publish(new PurchaseOrderCompletedEvent(command.id()));
    }

    public void handle(CancelPurchaseOrderCommand command) {
        PurchaseOrderAgg agg = repository.findById(command.id());
        agg.cancel();
        repository.save(agg);
        publisher.publish(new PurchaseOrderCancelledEvent(command.id()));
    }
}
