package org.scm.tms.application.event;

import org.scm.common.DomainEvent;

public record LogisticsAssignedEvent(String logisticsNo) implements DomainEvent {
    public String topic() { return "logistics-order"; }
    public String type() { return "LogisticsAssigned"; }
}