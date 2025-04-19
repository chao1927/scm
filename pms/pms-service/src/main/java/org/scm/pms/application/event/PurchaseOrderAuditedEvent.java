package org.scm.pms.application.event;

import org.scm.common.DomainEvent;

public record PurchaseOrderAuditedEvent(Long orderId) implements DomainEvent {
    public String topic() { return "purchase-order"; }
    public String type() { return "PurchaseOrderAudited"; }
}