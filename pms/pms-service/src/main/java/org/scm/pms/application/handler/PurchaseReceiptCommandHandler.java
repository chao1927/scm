package org.scm.pms.application.handler;

import org.scm.common.EventPublisher;
import org.scm.pms.application.command.BeginReceiveCommand;
import org.scm.pms.application.command.FinishReceiveCommand;
import org.scm.pms.application.command.ForceCompleteReceiveCommand;
import org.scm.pms.domain.model.PurchaseReceiptAgg;
import org.scm.pms.application.event.AllReceivedEvent;
import org.scm.pms.application.event.ReceivingForceCompletedEvent;
import org.scm.pms.application.event.ReceivingStartedEvent;
import org.scm.pms.domain.repository.PurchaseReceiptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PurchaseReceiptCommandHandler {

    @Autowired
    private PurchaseReceiptRepository receiptRepository;

    @Autowired
    private EventPublisher publisher;

    public void handle(BeginReceiveCommand command) {
        PurchaseReceiptAgg agg = receiptRepository.findById(command.receiverId());
        agg.beginReceive();
        receiptRepository.save(agg);
        publisher.publish(new ReceivingStartedEvent(command.receiptNo()));
    }

    public void handle(FinishReceiveCommand command) {
        PurchaseReceiptAgg agg = receiptRepository.findByReceiptNo(command.receiptNo());
        agg.finishReceive();
        receiptRepository.save(agg);
        publisher.publish(new AllReceivedEvent(command.receiptNo()));
    }

    public void handle(ForceCompleteReceiveCommand command) {
        PurchaseReceiptAgg agg = receiptRepository.findByReceiptNo(command.receiptNo());
        agg.forceComplete();
        receiptRepository.save(agg);
        publisher.publish(new ReceivingForceCompletedEvent(command.receiptNo()));
    }
}
