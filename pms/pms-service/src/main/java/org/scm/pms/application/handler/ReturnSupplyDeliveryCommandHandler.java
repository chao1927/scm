package org.scm.pms.application.handler;

import org.scm.common.EventPublisher;
import org.scm.common.exception.BizException;
import org.scm.pms._share.enums.ReturnSupplyDeliveryErrorCode;
import org.scm.pms.application.command.ConfirmSupplierReceivedCommand;
import org.scm.pms.application.command.CreateReturnSupplyDeliveryCommand;
import org.scm.pms.application.command.DispatchReturnSupplyCommand;
import org.scm.pms.application.command.MarkReturnSupplyInTransitCommand;
import org.scm.pms.application.event.ReturnSupplyDispatchedEvent;
import org.scm.pms.application.event.ReturnSupplyInTransitEvent;
import org.scm.pms.application.event.ReturnSupplyReceivedEvent;
import org.scm.pms.domain.model.ReturnSupplyDeliveryAgg;
import org.scm.pms.domain.repository.ReturnSupplyDeliveryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReturnSupplyDeliveryCommandHandler {

    @Autowired
    private ReturnSupplyDeliveryRepository repository;
    @Autowired
    private EventPublisher publisher;

    public void handle(CreateReturnSupplyDeliveryCommand command) {
        ReturnSupplyDeliveryAgg agg = ReturnSupplyDeliveryAgg.create(command);
        repository.save(agg);
        publisher.publish(new ReturnSupplyDispatchedEvent(command.deliveryNo()));
    }

    public void handle(DispatchReturnSupplyCommand command) {
        ReturnSupplyDeliveryAgg agg = repository.findByDeliveryNo(command.deliveryNo());
        if (agg == null) throw new BizException(ReturnSupplyDeliveryErrorCode.DELIVERY_NOT_FOUND);
        agg.dispatch();
        repository.save(agg);
        publisher.publish(new ReturnSupplyDispatchedEvent(command.deliveryNo()));
    }

    public void handle(MarkReturnSupplyInTransitCommand command) {
        ReturnSupplyDeliveryAgg agg = repository.findByDeliveryNo(command.deliveryNo());
        if (agg == null) throw new BizException(ReturnSupplyDeliveryErrorCode.DELIVERY_NOT_FOUND);
        agg.markInTransit();
        repository.save(agg);
        publisher.publish(new ReturnSupplyInTransitEvent(command.deliveryNo()));
    }

    public void handle(ConfirmSupplierReceivedCommand command) {
        ReturnSupplyDeliveryAgg agg = repository.findByDeliveryNo(command.deliveryNo());
        if (agg == null) throw new BizException(ReturnSupplyDeliveryErrorCode.DELIVERY_NOT_FOUND);
        agg.confirmReceived();
        repository.save(agg);
        publisher.publish(new ReturnSupplyReceivedEvent(command.deliveryNo()));
    }
}
