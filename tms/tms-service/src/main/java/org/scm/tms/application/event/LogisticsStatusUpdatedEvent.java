package org.scm.tms.application.event;

import org.scm.common.DomainEvent;

public record LogisticsStatusUpdatedEvent(String logisticsNo, Integer status) implements DomainEvent {
    public String topic() { return "logistics-order"; }
    public String type() { return "LogisticsStatusUpdated"; }
}