package org.scm.srm.wms.application.handler;

import org.scm.common.EventPublisher;
import org.scm.srm.wms.application.command.CompleteSalesOutboundCommand;
import org.scm.srm.wms.application.command.CreateSalesOutboundOrderCommand;
import org.scm.srm.wms.application.event.SalesOutboundCompletedEvent;
import org.scm.srm.wms.application.event.SalesOutboundOrderCreatedEvent;
import org.scm.srm.wms.domain.model.SalesOutboundOrderAgg;
import org.scm.srm.wms.domain.repository.SalesOutboundOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SalesOutboundOrderCommandHandler {

    @Autowired
    private SalesOutboundOrderRepository repository;

    @Autowired
    private EventPublisher publisher;

    public void handle(CreateSalesOutboundOrderCommand command) {
        SalesOutboundOrderAgg agg = SalesOutboundOrderAgg.create(command);
        repository.save(agg);
        publisher.publish(new SalesOutboundOrderCreatedEvent(command.outboundNo()));
    }

    public void handle(CompleteSalesOutboundCommand command) {
        SalesOutboundOrderAgg agg = repository.findByOutboundNo(command.outboundNo());
        agg.completeOutbound();
        repository.save(agg);
        publisher.publish(new SalesOutboundCompletedEvent(command.outboundNo()));
    }
}
