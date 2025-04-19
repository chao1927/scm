package org.scm.pms.application.handler;

import org.scm.common.EventPublisher;
import org.scm.pms.application.command.AuditPurchaseApplyCommand;
import org.scm.pms.application.command.CancelPurchaseApplyCommand;
import org.scm.pms.application.command.SubmitPurchaseApplyCommand;
import org.scm.pms.application.event.PurchaseApplyAuditedEvent;
import org.scm.pms.application.event.PurchaseApplyCancelledEvent;
import org.scm.pms.application.event.PurchaseApplyRejectedEvent;
import org.scm.pms.application.event.PurchaseApplySubmittedEvent;
import org.scm.pms.domain.model.PurchaseApplyAgg;
import org.scm.pms.domain.repository.PurchaseApplyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PurchaseApplyCommandHandler {

    @Autowired
    private PurchaseApplyRepository repository;

    @Autowired
    private EventPublisher publisher;

    public void handle(SubmitPurchaseApplyCommand command) {
        PurchaseApplyAgg agg = PurchaseApplyAgg.submit(command);
        repository.save(agg);
        publisher.publish(new PurchaseApplySubmittedEvent(agg.getId()));
    }

    public void handle(AuditPurchaseApplyCommand command) {
        PurchaseApplyAgg agg = repository.findById(command.id());
        agg.audit(command.approved(), command.reason());
        repository.save(agg);
        if (command.approved()) {
            publisher.publish(new PurchaseApplyAuditedEvent(agg.getId(), true));
        } else {
            publisher.publish(new PurchaseApplyRejectedEvent(agg.getId(), command.reason()));
        }
    }

    public void handle(CancelPurchaseApplyCommand command) {
        PurchaseApplyAgg agg = repository.findById(command.id());
        agg.cancel();
        repository.save(agg);
        publisher.publish(new PurchaseApplyCancelledEvent(agg.getId()));
    }
}
