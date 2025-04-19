package org.scm.oms.application.event;

import org.scm.common.DomainEvent;

public record SalesReturnOrderStatusChangedEvent(String salesReturnNo, Integer status) implements DomainEvent {
    @Override public String topic() { return "sales-return"; }
    @Override public String type() { return "SalesReturnOrderStatusChanged"; }
}