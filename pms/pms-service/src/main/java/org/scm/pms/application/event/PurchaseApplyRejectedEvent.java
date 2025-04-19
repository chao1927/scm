package org.scm.pms.application.event;

import org.scm.common.DomainEvent;

public record PurchaseApplyRejectedEvent(Long applyId, String reason) implements DomainEvent {
    @Override public String topic() { return "purchase-apply-topic"; }
    @Override public String type() { return "PurchaseApplyRejected"; }
}