package org.scm.srm.application.event;

import org.scm.common.DomainEvent;

public record SupplierDispatchedEvent(String deliveryNo) implements DomainEvent {
    public String topic() { return "supplier-delivery"; }
    public String type() { return "SupplierDispatched"; }
}