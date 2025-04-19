package org.scm.pms.application.event;

import org.scm.common.DomainEvent;

public record PurchaseApplyAuditedEvent(Long applyId, Boolean approved) implements DomainEvent {
    @Override public String topic() { return "purchase-apply-topic"; }
    @Override public String type() { return "PurchaseApplyAudited"; }
}