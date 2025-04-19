package org.scm.oms.application.event;

import org.scm.common.DomainEvent;

public record DeliveryStatusChangedEvent(String deliveryNo, Integer status) implements DomainEvent {
    @Override
    public String topic() { return "delivery-topic"; }
    @Override
    public String type() { return "DeliveryStatusChanged"; }
}