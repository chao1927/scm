package org.scm.pms.application.handler;

import org.scm.common.EventPublisher;
import org.scm.common.exception.BizException;
import org.scm.pms._share.enums.ReturnSupplyOrderErrorCode;
import org.scm.pms.application.command.CancelReturnSupplyOrderCommand;
import org.scm.pms.application.command.ConfirmReturnSupplyOrderCommand;
import org.scm.pms.application.command.CreateReturnSupplyOrderCommand;
import org.scm.pms.application.event.ReturnSupplyOrderCancelledEvent;
import org.scm.pms.application.event.ReturnSupplyOrderConfirmedEvent;
import org.scm.pms.application.event.ReturnSupplyOrderCreatedEvent;
import org.scm.pms.domain.model.ReturnSupplyOrderAgg;
import org.scm.pms.domain.repository.ReturnSupplyOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReturnSupplyOrderCommandHandler {

    @Autowired
    private ReturnSupplyOrderRepository repository;
    @Autowired
    private EventPublisher publisher;

    public void handle(CreateReturnSupplyOrderCommand command) {
        ReturnSupplyOrderAgg agg = ReturnSupplyOrderAgg.create(command);
        repository.save(agg);
        publisher.publish(new ReturnSupplyOrderCreatedEvent(command.orderNo()));
    }

    public void handle(ConfirmReturnSupplyOrderCommand command) {
        ReturnSupplyOrderAgg agg = repository.findByOrderNo(command.orderNo());
        if (agg == null) throw new BizException(ReturnSupplyOrderErrorCode.ORDER_NOT_FOUND);
        agg.confirm();
        repository.save(agg);
        publisher.publish(new ReturnSupplyOrderConfirmedEvent(command.orderNo()));
    }

    public void handle(CancelReturnSupplyOrderCommand command) {
        ReturnSupplyOrderAgg agg = repository.findByOrderNo(command.orderNo());
        if (agg == null) throw new BizException(ReturnSupplyOrderErrorCode.ORDER_NOT_FOUND);
        agg.cancel();
        repository.save(agg);
        publisher.publish(new ReturnSupplyOrderCancelledEvent(command.orderNo()));
    }
}
