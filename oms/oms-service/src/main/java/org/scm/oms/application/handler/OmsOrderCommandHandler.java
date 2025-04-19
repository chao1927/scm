package org.scm.oms.application.handler;

import org.scm.common.EventPublisher;
import org.scm.oms.application.command.*;
import org.scm.oms.application.event.*;
import org.scm.oms.domain.model.OmsOrderAgg;
import org.scm.oms.domain.repository.OmsOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OmsOrderCommandHandler {

    @Autowired
    private OmsOrderRepository repository;

    @Autowired
    private EventPublisher publisher;

    public void handle(CreateOmsOrderCommand cmd) {
        OmsOrderAgg omsOrderAgg = OmsOrderAgg.create(cmd);
        repository.save(omsOrderAgg);
        publisher.publish(new OmsOrderCreatedEvent(cmd.omsOrderNo()));
    }

    public void handle(AuditOmsOrderCommand cmd) {
        OmsOrderAgg agg = repository.findByOrderNo(cmd.omsOrderNo());
        agg.audit();
        repository.save(agg);
        publisher.publish(new OmsOrderAuditedEvent(cmd.omsOrderNo()));
    }

    public void handle(SplitOmsOrderCommand cmd) {
        OmsOrderAgg agg = repository.findByOrderNo(cmd.omsOrderNo());
        agg.splitOrder();
        repository.save(agg);
        publisher.publish(new OrderSplitEvent(cmd.omsOrderNo()));
    }

    public void handle(AllocateWarehouseCommand cmd) {
        OmsOrderAgg agg = repository.findByOrderNo(cmd.omsOrderNo());
        agg.allocateWarehouse();
        repository.save(agg);
        publisher.publish(new OrderAllocatedEvent(cmd.omsOrderNo()));
    }

    public void handle(UpdateOmsOrderStatusCommand cmd) {
        OmsOrderAgg agg = repository.findByOrderNo(cmd.omsOrderNo());
        agg.updateStatus(cmd.status());
        repository.save(agg);
        publisher.publish(new OmsOrderStatusChangedEvent(cmd.omsOrderNo(), cmd.status()));
    }
}
