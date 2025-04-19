package org.scm.srm.wms.application.event;

import org.scm.common.DomainEvent;

public record StorageShelvedEvent(String storageNo) implements DomainEvent {
    @Override public String topic() { return "storage-topic"; }
    @Override public String type() { return "StorageShelved"; }
}