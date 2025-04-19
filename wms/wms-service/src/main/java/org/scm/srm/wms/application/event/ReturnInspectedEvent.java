package org.scm.srm.wms.application.event;

import org.scm.common.DomainEvent;

public record ReturnInspectedEvent(String inboundNo) implements DomainEvent {
    @Override public String topic() { return "return-inbound"; }
    @Override public String type() { return "ReturnInspected"; }
}