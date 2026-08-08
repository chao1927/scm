package com.chaobo.scm.tms.application;

import com.chaobo.scm.tms.infrastructure.persistence.TmsInboundProjectionMapper;
import com.chaobo.scm.tms.infrastructure.persistence.TrackingMapper;
import com.chaobo.scm.tms.infrastructure.persistence.TransportTaskMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** TMS RocketMQ 入站业务路由回归测试。 */
class TmsInboundEventApplicationServiceTest {

    @Test
    void masterDataEventCreatesProjectionAndDuplicateIsIdempotent() throws Exception {
        MemoryInbox inbox = new MemoryInbox();
        Map<String, TmsInboundProjectionMapper.ProjectionRow> projections =
            new LinkedHashMap<>();
        TmsInboundEventApplicationService service = new TmsInboundEventApplicationService(
            inbox, null, new ObjectMapper(), null, null,
            row -> projections.put(row.projectionType() + ':' + row.objectKey(), row));
        var event = new TmsInboundEventApplicationService.EventEnvelope(
            "MDM", "evt-carrier-1", "CarrierEnabled", "CARRIER-01",
            new ObjectMapper().readTree("{\"carrierCode\":\"CARRIER-01\"}"));

        service.consume(event);
        service.consume(event);

        assertThat(inbox.events.get("evt-carrier-1").status()).isEqualTo(2);
        assertThat(projections.values()).singleElement().satisfies(row -> {
            assertThat(row.projectionType()).isEqualTo("MASTER_DATA_SNAPSHOT");
            assertThat(row.status()).isEqualTo("ACTIVE");
        });
    }

    private static final class MemoryInbox implements TrackingMapper {
        private final Map<String, EventInboxRow> events = new LinkedHashMap<>();

        @Override public TrackRow findTrackDuplicate(String waybillNo, String nodeCode, LocalDateTime trackAt) { return null; }
        @Override public List<TrackRow> listTracks(String waybillNo) { return List.of(); }
        @Override public void insertTrack(TrackRow row) { }
        @Override public ReceiptRow findReceiptByWaybill(String waybillNo) { return null; }
        @Override public ReceiptRow findReceipt(String receiptNo) { return null; }
        @Override public void insertReceipt(ReceiptRow row) { }
        @Override public int claimEvent(EventInboxRow row) {
            if (events.putIfAbsent(row.eventId(), row) != null) {
                return 0;
            }
            return 1;
        }
        @Override public EventInboxRow findEvent(String eventId) { return events.get(eventId); }
        @Override public int reclaimFailedEvent(String eventId) { return 0; }
        @Override public void updateEvent(EventInboxRow row) { events.put(row.eventId(), row); }
        @Override public void insertOutbox(TransportTaskMapper.OutboxRow row) { }
        @Override public List<TransportTaskMapper.OutboxRow> listOutbox() { return List.of(); }
        @Override public void insertOperationLog(TransportTaskMapper.OperationLogRow row) { }
        @Override public List<TransportTaskMapper.OperationLogRow> listOperationLogs() { return List.of(); }
    }
}
