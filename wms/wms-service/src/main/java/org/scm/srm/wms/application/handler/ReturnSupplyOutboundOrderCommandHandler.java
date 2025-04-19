package org.scm.srm.wms.application.handler;

import org.scm.common.EventPublisher;
import org.scm.common.exception.BizException;
import org.scm.srm.wms._share.enums.ReturnSupplyOutboundErrorCode;
import org.scm.srm.wms.application.command.CompleteReturnSupplyOutboundCommand;
import org.scm.srm.wms.application.command.StartReturnSupplyOutboundCommand;
import org.scm.srm.wms.application.event.ReturnSupplyOutboundCompletedEvent;
import org.scm.srm.wms.application.event.ReturnSupplyOutboundStartedEvent;
import org.scm.srm.wms.domain.model.ReturnSupplyOutboundOrderAgg;
import org.scm.srm.wms.domain.repository.ReturnSupplyOutboundOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReturnSupplyOutboundOrderCommandHandler {

    @Autowired
    private ReturnSupplyOutboundOrderRepository repository;
    @Autowired private EventPublisher eventPublisher;

    public void handle(StartReturnSupplyOutboundCommand command) {
        ReturnSupplyOutboundOrderAgg agg = ReturnSupplyOutboundOrderAgg.create(command);
        repository.save(agg);
        eventPublisher.publish(new ReturnSupplyOutboundStartedEvent(command.outboundNo()));
    }

    public void handle(CompleteReturnSupplyOutboundCommand command) {
        ReturnSupplyOutboundOrderAgg agg = repository.findByOutboundNo(command.outboundNo());
        if (agg == null) throw new BizException(ReturnSupplyOutboundErrorCode.OUTBOUND_NOT_FOUND);
        agg.completeOutbound();
        repository.save(agg);
        eventPublisher.publish(new ReturnSupplyOutboundCompletedEvent(command.outboundNo()));
    }
}
