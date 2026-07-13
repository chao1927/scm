package com.chaobo.scm.tms.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TrackingAggregateTest {
    @Test
    void appendArrivalPublishesTrackingAndArrivedEvents() {
        TrackingAggregate tracking = TrackingAggregate.append("TRK1", "WB1", "ARRIVED", "到达目的地",
                "杭州", LocalDateTime.parse("2026-07-12T10:00:00"), "CARRIER:SF", "evt-1");

        assertThat(tracking.pullEvents()).extracting(TmsEvent::eventType)
                .containsExactly("TrackingAppended", "TransportArrived");
    }

    @Test
    void supplementPublishesSupplementedEvent() {
        TrackingAggregate tracking = TrackingAggregate.supplement("TRK1", "WB1", "IN_TRANSIT", "人工补录在途",
                "嘉兴", LocalDateTime.parse("2026-07-12T11:00:00"), "承运商漏推");

        assertThat(tracking.sourceType()).isEqualTo("MANUAL");
        assertThat(tracking.pullEvents()).extracting(TmsEvent::eventType)
                .containsExactly("TrackingSupplemented");
    }
}
