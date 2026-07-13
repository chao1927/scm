package com.chaobo.scm.tms.application;

import com.chaobo.scm.tms.infrastructure.persistence.TrackingMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CarrierCallbackApplicationService {
    private final TrackingMapper mapper;
    private final TrackingApplicationService trackingService;
    private final DeliveryReceiptApplicationService receiptService;

    public CarrierCallbackApplicationService(TrackingMapper mapper, TrackingApplicationService trackingService,
                                             DeliveryReceiptApplicationService receiptService) {
        this.mapper = mapper;
        this.trackingService = trackingService;
        this.receiptService = receiptService;
    }

    @Transactional
    public void consume(CarrierEvent event) {
        int claimed = mapper.claimEvent(new TrackingMapper.EventInboxRow(event.eventId(), event.eventType(),
                event.waybillNo(), event.payload(), 1, null));
        if (claimed == 0) {
            return;
        }
        try {
            switch (event.eventType()) {
                case "TRACK" -> trackingService.append(new TrackingApplicationService.AppendCommand(event.waybillNo(),
                        event.nodeCode(), event.description(), event.location(), event.occurredAt(),
                        "CARRIER:" + event.carrierCode(), event.eventId(), event.operatorId(), event.eventId()));
                case "SIGNED", "REJECTED", "PARTIAL_SIGNED" -> receiptService.record(
                        new DeliveryReceiptApplicationService.RecordCommand(event.waybillNo(), event.receiptResult(),
                                event.signedBy(), event.occurredAt(), event.rejectReason(), event.proofUrl(),
                                event.operatorId(), event.eventId()));
                default -> throw new IllegalArgumentException("unsupported carrier event: " + event.eventType());
            }
            mapper.updateEvent(new TrackingMapper.EventInboxRow(event.eventId(), event.eventType(), event.waybillNo(),
                    event.payload(), 2, null));
        } catch (RuntimeException exception) {
            mapper.updateEvent(new TrackingMapper.EventInboxRow(event.eventId(), event.eventType(), event.waybillNo(),
                    event.payload(), 3, exception.getMessage()));
            throw exception;
        }
    }

    public record CarrierEvent(String eventId, String eventType, String carrierCode, String waybillNo,
                               String nodeCode, String description, String location, java.time.LocalDateTime occurredAt,
                               int receiptResult, String signedBy, String rejectReason, String proofUrl,
                               Long operatorId, String payload) {}
}
