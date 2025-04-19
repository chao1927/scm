package org.scm.pms.application.event;

import org.scm.common.DomainEvent;

public record PurchaseOrderCreatedEvent(Long orderId, String orderNo) implements DomainEvent {
    public String topic() { return "purchase-order"; }
    public String type() { return "PurchaseOrderCreated"; }
}