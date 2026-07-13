package com.chaobo.scm.tms.application;

import com.chaobo.scm.tms.domain.TmsEvent;
import com.chaobo.scm.tms.domain.TrackingAggregate;
import com.chaobo.scm.tms.domain.WaybillAggregate;
import com.chaobo.scm.tms.infrastructure.persistence.TrackingMapper;
import com.chaobo.scm.tms.infrastructure.persistence.TransportTaskMapper;
import com.chaobo.scm.tms.infrastructure.persistence.WaybillMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TrackingApplicationService {
    private final TrackingMapper mapper;
    private final WaybillApplicationService waybillService;
    private final AtomicLong sequence = new AtomicLong(100000);

    public TrackingApplicationService(TrackingMapper mapper, WaybillApplicationService waybillService) {
        this.mapper = mapper;
        this.waybillService = waybillService;
    }

    @Transactional
    public TrackingMapper.TrackRow append(AppendCommand command) {
        ensureActiveWaybill(command.waybillNo());
        TrackingMapper.TrackRow existing = mapper.findTrackDuplicate(command.waybillNo(), command.nodeCode(),
                command.trackAt());
        if (existing != null) {
            return existing;
        }
        TrackingAggregate aggregate = TrackingAggregate.append("TRK" + sequence.incrementAndGet(),
                command.waybillNo(), command.nodeCode(), command.description(), command.location(),
                command.trackAt(), command.sourceType(), command.rawEventId());
        TrackingMapper.TrackRow row = toRow(aggregate);
        mapper.insertTrack(row);
        saveEvents(aggregate.pullEvents());
        log("APPEND_TRACKING", command.waybillNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public TrackingMapper.TrackRow supplement(String waybillNo, SupplementCommand command) {
        ensureActiveWaybill(waybillNo);
        TrackingAggregate aggregate = TrackingAggregate.supplement("TRK" + sequence.incrementAndGet(), waybillNo,
                command.nodeCode(), command.description(), command.location(), command.trackAt(), command.reason());
        TrackingMapper.TrackRow row = toRow(aggregate);
        mapper.insertTrack(row);
        saveEvents(aggregate.pullEvents());
        log("SUPPLEMENT_TRACKING", waybillNo, command.operatorId(), command.idempotencyKey());
        return row;
    }

    public List<TrackingMapper.TrackRow> list(String waybillNo) {
        return mapper.listTracks(waybillNo);
    }

    private void ensureActiveWaybill(String waybillNo) {
        WaybillMapper.WaybillRow waybill = waybillService.get(waybillNo);
        if (waybill == null) {
            throw new IllegalArgumentException("waybill not found");
        }
        if (waybill.status() == WaybillAggregate.VOIDED) {
            throw new IllegalStateException("voided waybill cannot receive tracking");
        }
    }

    private TrackingMapper.TrackRow toRow(TrackingAggregate aggregate) {
        return new TrackingMapper.TrackRow(null, aggregate.trackNo(), aggregate.waybillNo(), aggregate.nodeCode(),
                aggregate.description(), aggregate.location(), aggregate.trackAt(), aggregate.sourceType(),
                aggregate.rawEventId(), aggregate.manualReason());
    }

    private void saveEvents(List<TmsEvent> events) {
        for (TmsEvent event : events) {
            mapper.insertOutbox(new TransportTaskMapper.OutboxRow(event.eventType(), event.businessNo(),
                    event.payload(), 1, event.occurredAt()));
        }
    }

    private void log(String operationType, String businessNo, Long operatorId, String idempotencyKey) {
        mapper.insertOperationLog(new TransportTaskMapper.OperationLogRow(operationType, businessNo, operatorId,
                idempotencyKey, LocalDateTime.now()));
    }

    public record AppendCommand(String waybillNo, String nodeCode, String description, String location,
                                LocalDateTime trackAt, String sourceType, String rawEventId, Long operatorId,
                                String idempotencyKey) {}

    public record SupplementCommand(String nodeCode, String description, String location, LocalDateTime trackAt,
                                    String reason, Long operatorId, String idempotencyKey) {}
}
