package com.chaobo.scm.tms.application;

import com.chaobo.scm.tms.domain.DeliveryReceiptAggregate;
import com.chaobo.scm.tms.domain.TmsEvent;
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
public class DeliveryReceiptApplicationService {
    private final TrackingMapper mapper;
    private final WaybillApplicationService waybillService;
    private final AtomicLong sequence = new AtomicLong(110000);

    public DeliveryReceiptApplicationService(TrackingMapper mapper, WaybillApplicationService waybillService) {
        this.mapper = mapper;
        this.waybillService = waybillService;
    }

    @Transactional
    public TrackingMapper.ReceiptRow record(RecordCommand command) {
        WaybillMapper.WaybillRow waybill = waybillService.get(command.waybillNo());
        if (waybill == null) {
            throw new IllegalArgumentException("waybill not found");
        }
        if (waybill.status() == WaybillAggregate.VOIDED) {
            throw new IllegalStateException("voided waybill cannot receive receipt");
        }
        TrackingMapper.ReceiptRow existing = mapper.findReceiptByWaybill(command.waybillNo());
        if (existing != null) {
            return existing;
        }
        DeliveryReceiptAggregate aggregate = DeliveryReceiptAggregate.record("RCP" + sequence.incrementAndGet(),
                command.waybillNo(), command.result(), command.signedBy(), command.signedAt(),
                command.rejectReason(), command.proofUrl());
        TrackingMapper.ReceiptRow row = toRow(aggregate);
        mapper.insertReceipt(row);
        saveEvents(aggregate.pullEvents());
        log("RECORD_DELIVERY_RECEIPT", command.waybillNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    public TrackingMapper.ReceiptRow get(String receiptNo) {
        return mapper.findReceipt(receiptNo);
    }

    private TrackingMapper.ReceiptRow toRow(DeliveryReceiptAggregate aggregate) {
        return new TrackingMapper.ReceiptRow(null, aggregate.receiptNo(), aggregate.waybillNo(), aggregate.result(),
                aggregate.signedBy(), aggregate.signedAt(), aggregate.rejectReason(), aggregate.proofUrl());
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

    public record RecordCommand(String waybillNo, int result, String signedBy, LocalDateTime signedAt,
                                String rejectReason, String proofUrl, Long operatorId, String idempotencyKey) {}
}
