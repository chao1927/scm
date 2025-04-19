package org.scm.pms.application.handler;

import org.scm.common.EventPublisher;
import org.scm.pms.domain.model.ReturnSupplyApplyAgg;
import org.scm.pms.application.command.AuditReturnSupplyApplyCommand;
import org.scm.pms.application.command.CancelReturnSupplyApplyCommand;
import org.scm.pms.application.command.SubmitReturnSupplyApplyCommand;
import org.scm.pms.application.event.ReturnSupplyApplyAuditedEvent;
import org.scm.pms.application.event.ReturnSupplyApplyCancelledEvent;
import org.scm.pms.application.event.ReturnSupplyApplySubmittedEvent;
import org.scm.pms.domain.repository.ReturnSupplyApplyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReturnSupplyApplyCommandHandler {

    @Autowired
    private ReturnSupplyApplyRepository repository;
    @Autowired
    private EventPublisher publisher;

    public void handle(SubmitReturnSupplyApplyCommand command) {
        ReturnSupplyApplyAgg agg = ReturnSupplyApplyAgg.create(command);
        repository.save(agg);
        publisher.publish(new ReturnSupplyApplySubmittedEvent(command.applyNo()));
    }

    public void handle(AuditReturnSupplyApplyCommand command) {
        ReturnSupplyApplyAgg agg = repository.findByApplyNo(command.applyNo());
        agg.audit(command.approved());
        repository.save(agg);
        publisher.publish(new ReturnSupplyApplyAuditedEvent(command.applyNo(), command.approved()));
    }

    public void handle(CancelReturnSupplyApplyCommand command) {
        ReturnSupplyApplyAgg agg = repository.findByApplyNo(command.applyNo());
        agg.cancel();
        repository.save(agg);
        publisher.publish(new ReturnSupplyApplyCancelledEvent(command.applyNo()));
    }
}
