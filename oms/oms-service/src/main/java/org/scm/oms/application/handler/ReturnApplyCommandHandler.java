package org.scm.oms.application.handler;

import org.scm.common.EventPublisher;
import org.scm.oms.application.command.AuditReturnApplyCommand;
import org.scm.oms.application.command.SubmitReturnApplyCommand;
import org.scm.oms.application.event.ReturnApplyAuditedEvent;
import org.scm.oms.application.event.ReturnApplySubmittedEvent;
import org.scm.oms.domain.model.ReturnApplyAgg;
import org.scm.oms.domain.repository.ReturnApplyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReturnApplyCommandHandler {

    @Autowired
    private ReturnApplyRepository repository;

    @Autowired
    private EventPublisher publisher;

    public void handle(SubmitReturnApplyCommand command) {
        ReturnApplyAgg agg = ReturnApplyAgg.create(command);
        repository.save(agg);
        publisher.publish(new ReturnApplySubmittedEvent(command.returnApplyNo()));
    }

    public void handle(AuditReturnApplyCommand command) {
        ReturnApplyAgg agg = repository.findByApplyNo(command.returnApplyNo());
        agg.audit(command.approved());
        repository.save(agg);
        publisher.publish(new ReturnApplyAuditedEvent(command.returnApplyNo(), command.approved()));
    }
}
