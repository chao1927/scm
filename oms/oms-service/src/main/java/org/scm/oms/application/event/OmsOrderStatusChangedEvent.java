package org.scm.oms.application.event;

import org.scm.common.DomainEvent;

public record OmsOrderStatusChangedEvent(String omsOrderNo, int status) implements DomainEvent {
    public String topic() { return "oms-order"; }
    public String type() { return "OmsOrderStatusChanged"; }
}