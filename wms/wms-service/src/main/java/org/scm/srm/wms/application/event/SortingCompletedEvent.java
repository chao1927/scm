package org.scm.srm.wms.application.event;

import org.scm.common.DomainEvent;

public record SortingCompletedEvent(Long sortingOrderId) implements DomainEvent {
    public String topic() { return "sorting-order-topic"; }
    public String type() { return "SortingCompleted"; }
}