package org.scm.ecs.application.event;

import org.scm.common.DomainEvent;

public record EcommOrderCreatedEvent(
        String platformOrderNo
) implements DomainEvent {
    @Override public String topic() { return "ecomm-order-topic"; }
    @Override public String type() { return "EcommOrderCreated"; }
}