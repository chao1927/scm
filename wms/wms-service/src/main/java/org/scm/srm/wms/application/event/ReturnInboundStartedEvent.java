package org.scm.srm.wms.application.event;

import org.scm.common.DomainEvent;

public record ReturnInboundStartedEvent(String inboundNo) implements DomainEvent {
    @Override public String topic() { return "return-inbound"; }
    @Override public String type() { return "ReturnInboundStarted"; }
}