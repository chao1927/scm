package org.scm.pms.application.event;

import org.scm.common.DomainEvent;

public record ReturnSupplyOrderConfirmedEvent(String orderNo) implements DomainEvent {
    @Override public String topic() { return "return-supply-topic"; }
    @Override public String type() { return "ReturnSupplyOrderConfirmed"; }
}