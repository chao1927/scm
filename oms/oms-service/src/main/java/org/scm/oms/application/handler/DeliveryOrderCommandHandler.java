package org.scm.oms.application.handler;

import org.scm.common.EventPublisher;
import org.scm.oms._share.enums.DeliveryOrderStatus;
import org.scm.oms.application.command.AssignLogisticsCommand;
import org.scm.oms.application.command.CreateDeliveryOrderCommand;
import org.scm.oms.application.command.UpdateDeliveryStatusCommand;
import org.scm.oms.application.event.DeliveryOrderCreatedEvent;
import org.scm.oms.application.event.DeliveryStatusChangedEvent;
import org.scm.oms.application.event.LogisticsAssignedEvent;
import org.scm.oms.domain.model.DeliveryOrderAgg;
import org.scm.oms.domain.repository.DeliveryOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeliveryOrderCommandHandler {

    @Autowired
    private DeliveryOrderRepository repository;

    @Autowired
    private EventPublisher eventPublisher;

    public void handle(CreateDeliveryOrderCommand command) {

        DeliveryOrderAgg agg = DeliveryOrderAgg.create(command);
        repository.save(agg);

        eventPublisher.publish(new DeliveryOrderCreatedEvent(command.deliveryNo()));
    }

    public void handle(UpdateDeliveryStatusCommand command) {
        DeliveryOrderAgg agg = repository.findByDeliveryNo(command.deliveryNo());

        repository.save(agg);
        eventPublisher.publish(new DeliveryStatusChangedEvent(command.deliveryNo(), command.newStatus()));
    }

    public void handle(AssignLogisticsCommand command) {
        DeliveryOrderAgg agg = repository.findByDeliveryNo(command.deliveryNo());
        agg.assignLogistics(command.logisticNo(), command.logisticChannelId());

        repository.save(agg);
        eventPublisher.publish(new LogisticsAssignedEvent(command.deliveryNo(), command.logisticNo()));
    }
}
