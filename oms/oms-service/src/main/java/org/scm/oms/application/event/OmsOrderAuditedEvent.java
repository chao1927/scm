package org.scm.oms.application.event;

import org.scm.common.DomainEvent;

public record OmsOrderAuditedEvent(String omsOrderNo) implements DomainEvent {
    public String topic() { return "oms-order"; }
    public String type() { return "OmsOrderAudited"; }
}