package org.scm.srm.wms.application.event;

import org.scm.common.DomainEvent;

public record SalesOutboundOrderCreatedEvent(String outboundNo) implements DomainEvent {
    public String topic() { return "sales-outbound-order"; }
    public String type() { return "SalesOutboundOrderCreated"; }
}