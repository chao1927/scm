package org.scm.oms.application.event;

import org.scm.common.DomainEvent;

public record DeliveryOrderCreatedEvent(String deliveryNo) implements DomainEvent {
    @Override
    public String topic() { return "delivery-topic"; }
    @Override
    public String type() { return "DeliveryOrderCreated"; }
}