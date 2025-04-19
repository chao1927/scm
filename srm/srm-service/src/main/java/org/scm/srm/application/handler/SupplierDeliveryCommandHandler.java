package org.scm.srm.application.handler;

import org.scm.common.EventPublisher;
import org.scm.srm.application.command.ConfirmArrivalCommand;
import org.scm.srm.application.command.SupplierDispatchCommand;
import org.scm.srm.application.event.GoodsArrivedAtWarehouseEvent;
import org.scm.srm.application.event.SupplierDispatchedEvent;
import org.scm.srm.domain.model.SupplierDeliveryOrderAgg;
import org.scm.srm.domain.repository.SupplierDeliveryOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SupplierDeliveryCommandHandler {

    @Autowired
    private SupplierDeliveryOrderRepository repository;

    @Autowired
    private EventPublisher eventPublisher;

    public void handle(SupplierDispatchCommand command) {
        SupplierDeliveryOrderAgg agg = repository.findByDeliveryNo(command.deliveryNo());
        agg.dispatch(command.deliveryTime(), command.logisticsNo());
        repository.save(agg);
        eventPublisher.publish(new SupplierDispatchedEvent(command.deliveryNo()));
    }

    public void handle(ConfirmArrivalCommand command) {
        SupplierDeliveryOrderAgg agg = repository.findByDeliveryNo(command.deliveryNo());
        agg.confirmArrival();
        repository.save(agg);
        eventPublisher.publish(new GoodsArrivedAtWarehouseEvent(command.deliveryNo()));
    }
}
