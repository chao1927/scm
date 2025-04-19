package org.scm.oms.application.event;

import org.scm.common.DomainEvent;

public record ReturnApplyAuditedEvent(String returnApplyNo, boolean approved) implements DomainEvent {
    public String topic() { return "return-apply"; }
    public String type() { return "ReturnApplyAudited"; }
}