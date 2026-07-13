package com.chaobo.scm.tms.application;

import com.chaobo.scm.tms.domain.DeliveryReceiptAggregate;
import com.chaobo.scm.tms.infrastructure.persistence.TrackingMapper;
import com.chaobo.scm.tms.infrastructure.persistence.TransportTaskMapper;
import com.chaobo.scm.tms.infrastructure.persistence.WaybillMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TrackingReceiptApplicationServiceTest {
    @Test
    void appendTrackRecordReceiptAndIgnoreDuplicateCarrierEvent() {
        Services services = servicesWithWaybill();
        LocalDateTime at = LocalDateTime.parse("2026-07-12T10:00:00");

        services.callbackService.consume(new CarrierCallbackApplicationService.CarrierEvent("evt-track-1",
                "TRACK", "SF", "WB800001", "ARRIVED", "到达杭州", "杭州", at,
                0, null, null, null, 1001L, "{}"));
        services.callbackService.consume(new CarrierCallbackApplicationService.CarrierEvent("evt-track-1",
                "TRACK", "SF", "WB800001", "ARRIVED", "重复到达", "杭州", at,
                0, null, null, null, 1001L, "{}"));
        services.callbackService.consume(new CarrierCallbackApplicationService.CarrierEvent("evt-sign-1",
                "SIGNED", "SF", "WB800001", null, null, null,
                LocalDateTime.parse("2026-07-12T12:00:00"), DeliveryReceiptAggregate.SIGNED, "李四",
                null, "oss://proof/RCP1.jpg", 1001L, "{}"));

        assertThat(services.trackingMapper.tracks).hasSize(1);
        assertThat(services.trackingMapper.receipts).hasSize(1);
        assertThat(services.trackingMapper.outbox).extracting(TransportTaskMapper.OutboxRow::eventType)
                .contains("TrackingAppended", "TransportArrived", "TransportSigned");
        assertThat(services.trackingMapper.inbox.get("evt-track-1").status()).isEqualTo(2);
    }

    @Test
    void supplementTrackThroughApplicationService() {
        Services services = servicesWithWaybill();

        services.trackingService.supplement("WB800001", new TrackingApplicationService.SupplementCommand(
                "IN_TRANSIT", "人工补录在途", "嘉兴", LocalDateTime.parse("2026-07-12T11:00:00"),
                "承运商漏推", 1001L, "idem-supplement"));

        assertThat(services.trackingService.list("WB800001")).hasSize(1);
        assertThat(services.trackingMapper.outbox).extracting(TransportTaskMapper.OutboxRow::eventType)
                .contains("TrackingSupplemented");
    }

    static Services servicesWithWaybill() {
        WaybillApplicationServiceTest.Services base = WaybillApplicationServiceTest.servicesWithAcceptedTask();
        WaybillMapper.WaybillRow waybill = base.waybillService().createFromTask("TMS700001",
                new WaybillApplicationService.CreateCommand("SF", "顺丰", "SF123",
                        "SF-EXPRESS", "ok", 1001L, "idem-wb"));
        if (!"WB800001".equals(waybill.waybillNo())) {
            throw new IllegalStateException("unexpected test waybill number");
        }
        MemoryTrackingMapper trackingMapper = new MemoryTrackingMapper();
        TrackingApplicationService trackingService = new TrackingApplicationService(trackingMapper, base.waybillService());
        DeliveryReceiptApplicationService receiptService = new DeliveryReceiptApplicationService(trackingMapper,
                base.waybillService());
        CarrierCallbackApplicationService callbackService = new CarrierCallbackApplicationService(trackingMapper,
                trackingService, receiptService);
        return new Services(trackingMapper, trackingService, receiptService, callbackService);
    }

    record Services(MemoryTrackingMapper trackingMapper, TrackingApplicationService trackingService,
                    DeliveryReceiptApplicationService receiptService,
                    CarrierCallbackApplicationService callbackService) {}

    public static class MemoryTrackingMapper implements TrackingMapper {
        final Map<String, TrackRow> tracks = new LinkedHashMap<>();
        final Map<String, ReceiptRow> receipts = new LinkedHashMap<>();
        final Map<String, EventInboxRow> inbox = new LinkedHashMap<>();
        final List<TransportTaskMapper.OutboxRow> outbox = new ArrayList<>();
        final List<TransportTaskMapper.OperationLogRow> logs = new ArrayList<>();

        @Override
        public TrackRow findTrackDuplicate(String waybillNo, String nodeCode, LocalDateTime trackAt) {
            return tracks.values().stream()
                    .filter(row -> row.waybillNo().equals(waybillNo))
                    .filter(row -> row.nodeCode().equals(nodeCode))
                    .filter(row -> row.trackAt().equals(trackAt))
                    .findFirst().orElse(null);
        }

        @Override
        public List<TrackRow> listTracks(String waybillNo) {
            return tracks.values().stream().filter(row -> row.waybillNo().equals(waybillNo)).toList();
        }

        @Override
        public void insertTrack(TrackRow row) { tracks.put(row.trackNo(), row); }

        @Override
        public ReceiptRow findReceiptByWaybill(String waybillNo) {
            return receipts.values().stream().filter(row -> row.waybillNo().equals(waybillNo)).findFirst().orElse(null);
        }

        @Override
        public ReceiptRow findReceipt(String receiptNo) { return receipts.get(receiptNo); }

        @Override
        public void insertReceipt(ReceiptRow row) { receipts.put(row.receiptNo(), row); }

        @Override
        public int claimEvent(EventInboxRow row) {
            if (inbox.containsKey(row.eventId())) {
                return 0;
            }
            inbox.put(row.eventId(), row);
            return 1;
        }

        @Override
        public void updateEvent(EventInboxRow row) { inbox.put(row.eventId(), row); }

        @Override
        public void insertOutbox(TransportTaskMapper.OutboxRow row) { outbox.add(row); }

        @Override
        public List<TransportTaskMapper.OutboxRow> listOutbox() { return outbox; }

        @Override
        public void insertOperationLog(TransportTaskMapper.OperationLogRow row) { logs.add(row); }

        @Override
        public List<TransportTaskMapper.OperationLogRow> listOperationLogs() { return logs; }
    }
}
