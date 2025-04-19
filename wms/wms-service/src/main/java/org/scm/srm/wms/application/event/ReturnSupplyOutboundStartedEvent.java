package org.scm.srm.wms.application.event;

import org.scm.common.DomainEvent;

public record ReturnSupplyOutboundStartedEvent(String outboundNo) implements DomainEvent {
    @Override public String topic() { return "return-supply-topic"; }
    @Override public String type() { return "ReturnSupplyOutboundStarted"; }
}