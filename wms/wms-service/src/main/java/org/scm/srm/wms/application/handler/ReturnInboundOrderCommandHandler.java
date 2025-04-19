package org.scm.srm.wms.application.handler;

import org.scm.common.EventPublisher;
import org.scm.srm.wms.application.command.CompleteReturnInboundCommand;
import org.scm.srm.wms.application.command.InspectReturnGoodsCommand;
import org.scm.srm.wms.application.command.ReceiveReturnInboundCommand;
import org.scm.srm.wms.application.event.ReturnInboundCompletedEvent;
import org.scm.srm.wms.application.event.ReturnInboundStartedEvent;
import org.scm.srm.wms.application.event.ReturnInspectedEvent;
import org.scm.srm.wms.domain.model.ReturnInboundOrderAgg;
import org.scm.srm.wms.domain.repository.ReturnInboundOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReturnInboundOrderCommandHandler {

    @Autowired
    private ReturnInboundOrderRepository repository;

    @Autowired
    private EventPublisher publisher;

    public void handle(ReceiveReturnInboundCommand command) {
        ReturnInboundOrderAgg agg = ReturnInboundOrderAgg.create(command);
        repository.save(agg);
        publisher.publish(new ReturnInboundStartedEvent(command.inboundNo()));
    }

    public void handle(InspectReturnGoodsCommand command) {
        ReturnInboundOrderAgg agg = repository.findById(Long.valueOf(command.inboundNo()));
        agg.inspect();
        repository.save(agg);
        publisher.publish(new ReturnInspectedEvent(command.inboundNo()));
    }

    public void handle(CompleteReturnInboundCommand command) {
        ReturnInboundOrderAgg agg = repository.findById(Long.valueOf(command.inboundNo()));
        agg.complete();
        repository.save(agg);
        publisher.publish(new ReturnInboundCompletedEvent(command.inboundNo()));
    }
}
