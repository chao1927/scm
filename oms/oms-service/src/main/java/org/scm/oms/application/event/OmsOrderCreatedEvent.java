package org.scm.oms.application.event;

import org.scm.common.DomainEvent;

public record OmsOrderCreatedEvent(String omsOrderNo) implements DomainEvent {
    public String topic() { return "oms-order"; }
    public String type() { return "OmsOrderCreated"; }
}