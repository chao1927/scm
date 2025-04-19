package org.scm.pms.application.event;

import org.scm.common.DomainEvent;

public record PurchaseApplySubmittedEvent(Long applyId) implements DomainEvent {
    @Override public String topic() { return "purchase-apply-topic"; }
    @Override public String type() { return "PurchaseApplySubmitted"; }
}