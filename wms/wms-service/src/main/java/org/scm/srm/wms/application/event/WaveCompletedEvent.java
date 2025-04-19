package org.scm.srm.wms.application.event;

import org.scm.common.DomainEvent;

public record WaveCompletedEvent(String waveNo) implements DomainEvent {
    public String topic() { return "wave-topic"; }
    public String type() { return "WaveCompleted"; }
}