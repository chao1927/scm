package org.scm.oms.application.event;

import org.scm.common.DomainEvent;

public record LogisticsAssignedEvent(String deliveryNo, String logisticNo) implements DomainEvent {
    @Override
    public String topic() { return "delivery-topic"; }
    @Override
    public String type() { return "LogisticsAssigned"; }
}