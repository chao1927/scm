package org.scm.ecs.application.handler;

import org.scm.common.EventPublisher;
import org.scm.ecs.adapter.infra.domain.EcommOrder;
import org.scm.ecs.application.command.CreateEcommOrderCommand;
import org.scm.ecs.application.command.UpdateEcommOrderStatusCommand;
import org.scm.ecs.application.event.EcommOrderCreatedEvent;
import org.scm.ecs.application.event.EcommOrderStatusChangedEvent;
import org.scm.ecs.domain.model.EcommOrderAgg;
import org.scm.ecs.domain.repository.EcommOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EcommOrderCommandHandler {

    @Autowired
    private EcommOrderRepository repository;

    @Autowired
    private EventPublisher publisher;

    public void handle(CreateEcommOrderCommand command) {
        EcommOrder order = new EcommOrder(
                command.platformOrderNo(),
                command.customerName(),
                command.customerPhone(),
                command.addressDetail(),
                command.totalProductAmount(),
                command.freight(),
                command.actualPayment(),
                command.paymentMethod(),
                command.paymentTradeNo(),
                1 // 初始状态为 待发货
        );
        repository.save(new EcommOrderAgg(order));
        publisher.publish(new EcommOrderCreatedEvent(command.platformOrderNo()));
    }

    public void handle(UpdateEcommOrderStatusCommand command) {
        EcommOrderAgg agg = repository.findById(command.id());
        agg.updateStatus(command);
        repository.save(agg);
        publisher.publish(new EcommOrderStatusChangedEvent(command.id(), command.newStatus()));
    }
}
