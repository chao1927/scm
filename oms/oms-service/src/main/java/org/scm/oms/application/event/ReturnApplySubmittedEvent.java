package org.scm.oms.application.event;

import org.scm.common.DomainEvent;

public record ReturnApplySubmittedEvent(String returnApplyNo) implements DomainEvent {
    public String topic() { return "return-apply"; }
    public String type() { return "ReturnApplySubmitted"; }
}