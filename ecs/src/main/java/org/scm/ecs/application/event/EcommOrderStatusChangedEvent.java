package org.scm.ecs.application.event;

import org.scm.common.DomainEvent;

public record EcommOrderStatusChangedEvent(
        Long orderId,
        Integer newStatus
) implements DomainEvent {
    @Override
    public String topic() { return "ecomm-order-topic"; }
    @Override
    public String type() { return "EcommOrderStatusChanged"; }
}