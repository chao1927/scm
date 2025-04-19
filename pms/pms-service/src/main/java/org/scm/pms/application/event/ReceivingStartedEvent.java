package org.scm.pms.application.event;

import org.scm.common.DomainEvent;

public record ReceivingStartedEvent(String receiptNo) implements DomainEvent {
    public String topic() { return "receipt-topic"; }
    public String type() { return "ReceivingStarted"; }
}