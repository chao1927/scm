package com.chaobo.scm.oms.application;

import com.chaobo.scm.oms.domain.CancellationRequestAggregate;
import com.chaobo.scm.oms.domain.FulfillmentAggregate;
import com.chaobo.scm.oms.domain.OmsEvent;
import com.chaobo.scm.oms.infrastructure.persistence.CancellationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CancellationApplicationService {
    private final CancellationMapper mapper;
    private final FulfillmentApplicationService fulfillmentService;
    private final AtomicLong sequence = new AtomicLong(500000);

    public CancellationApplicationService(CancellationMapper mapper, FulfillmentApplicationService fulfillmentService) {
        this.mapper = mapper;
        this.fulfillmentService = fulfillmentService;
    }

    @Transactional
    public CancellationMapper.CancelRow create(CreateCommand command) {
        FulfillmentMapperView fulfillment = new FulfillmentMapperView(fulfillmentService.getFulfillment(command.fulfillmentNo()));
        if (fulfillment.status() == FulfillmentAggregate.SHIPPED) {
            throw new IllegalStateException("shipped fulfillment must use after-sale");
        }
        CancellationMapper.CancelRow existing = mapper.findCancelByFulfillment(command.fulfillmentNo());
        if (existing != null) {
            return existing;
        }
        String cancellationNo = "CAN" + sequence.incrementAndGet();
        CancellationRequestAggregate aggregate = CancellationRequestAggregate.create(cancellationNo,
                fulfillment.salesOrderNo(), command.fulfillmentNo(), fulfillment.outboundNo(),
                fulfillment.reservationRefNo(), command.reason());
        CancellationMapper.CancelRow row = toRow(aggregate);
        mapper.insertCancel(row);
        saveEvents(aggregate.pullEvents());
        log("CREATE_CANCEL_REQUEST", cancellationNo, command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public CancellationMapper.CancelRow approve(String cancellationNo, ApproveCommand command) {
        CancellationRequestAggregate aggregate = load(cancellationNo);
        aggregate.approve(command.remark());
        mapper.updateCancel(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("APPROVE_CANCEL_REQUEST", cancellationNo, command.operatorId(), command.idempotencyKey());
        return mapper.findCancel(cancellationNo);
    }

    @Transactional
    public CancellationMapper.CancelRow process(String cancellationNo, ProcessCommand command) {
        CancellationRequestAggregate aggregate = load(cancellationNo);
        boolean requiresWms = aggregate.outboundNo() != null && !aggregate.outboundNo().isBlank();
        aggregate.process(requiresWms);
        mapper.updateCancel(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        if (requiresWms) {
            fulfillmentService.cancelOutbound(aggregate.outboundNo(),
                    new FulfillmentApplicationService.CancelOutboundCommand(aggregate.reason(),
                            command.operatorId(), command.idempotencyKey()));
        } else if (aggregate.reservationRefNo() != null && !aggregate.reservationRefNo().isBlank()) {
            mapper.insertIntegrationCommand(new CancellationMapper.IntegrationCommandRow("ReleaseInventory",
                    "INVENTORY", cancellationNo, cancellationNo + ":RELEASE", aggregate.reservationRefNo()));
        } else {
            aggregate.markStockReleased();
            mapper.updateCancel(toRow(aggregate));
            saveEvents(aggregate.pullEvents());
        }
        log("PROCESS_CANCEL_REQUEST", cancellationNo, command.operatorId(), command.idempotencyKey());
        return mapper.findCancel(cancellationNo);
    }

    @Transactional
    public void consumeEvent(CancellationEvent event) {
        int claimed = mapper.claimEvent(new CancellationMapper.EventInboxRow(event.eventId(), event.eventType(),
                event.businessNo(), event.payload(), 1, null));
        if (claimed == 0) {
            return;
        }
        try {
            CancellationRequestAggregate aggregate = switch (event.eventType()) {
                case "WmsOutboundCancelled" -> loadByOutbound(event.outboundNo());
                case "StockReleased" -> loadByReservation(event.reservationRefNo());
                default -> throw new IllegalArgumentException("unsupported cancellation event: " + event.eventType());
            };
            if (aggregate == null) {
                mapper.updateEvent(new CancellationMapper.EventInboxRow(event.eventId(), event.eventType(),
                        event.businessNo(), event.payload(), 2, null));
                return;
            }
            if (event.eventType().equals("WmsOutboundCancelled")) {
                aggregate.markWmsCancelled();
            } else {
                aggregate.markStockReleased();
            }
            mapper.updateCancel(toRow(aggregate));
            saveEvents(aggregate.pullEvents());
            mapper.updateEvent(new CancellationMapper.EventInboxRow(event.eventId(), event.eventType(),
                    event.businessNo(), event.payload(), 2, null));
        } catch (RuntimeException exception) {
            mapper.updateEvent(new CancellationMapper.EventInboxRow(event.eventId(), event.eventType(),
                    event.businessNo(), event.payload(), 3, exception.getMessage()));
            throw exception;
        }
    }

    public CancellationMapper.CancelRow get(String cancellationNo) { return mapper.findCancel(cancellationNo); }

    private CancellationRequestAggregate load(String cancellationNo) {
        CancellationMapper.CancelRow row = mapper.findCancel(cancellationNo);
        if (row == null) {
            throw new IllegalArgumentException("cancellation request not found");
        }
        return fromRow(row);
    }

    private CancellationRequestAggregate loadByOutbound(String outboundNo) {
        if (outboundNo == null || outboundNo.isBlank()) {
            return null;
        }
        CancellationMapper.CancelRow row = mapper.findCancelByOutbound(outboundNo);
        if (row == null) {
            return null;
        }
        return fromRow(row);
    }

    private CancellationRequestAggregate loadByReservation(String reservationRefNo) {
        if (reservationRefNo == null || reservationRefNo.isBlank()) {
            return null;
        }
        CancellationMapper.CancelRow row = mapper.findCancelByReservation(reservationRefNo);
        if (row == null) {
            return null;
        }
        return fromRow(row);
    }

    private CancellationRequestAggregate fromRow(CancellationMapper.CancelRow row) {
        return CancellationRequestAggregate.restore(row.cancellationNo(), row.salesOrderNo(), row.fulfillmentNo(),
                row.outboundNo(), row.reservationRefNo(), row.reason(), row.status(), row.wmsCancelled(),
                row.stockReleased(), row.version());
    }

    private CancellationMapper.CancelRow toRow(CancellationRequestAggregate aggregate) {
        return new CancellationMapper.CancelRow(aggregate.cancellationNo(), aggregate.salesOrderNo(),
                aggregate.fulfillmentNo(), aggregate.outboundNo(), aggregate.reservationRefNo(), aggregate.reason(),
                aggregate.status(), aggregate.wmsCancelled(), aggregate.stockReleased(), aggregate.version());
    }

    private void saveEvents(List<OmsEvent> events) {
        for (OmsEvent event : events) {
            mapper.insertOutbox(new CancellationMapper.OutboxRow(event.eventType(), event.businessNo(),
                    event.payload(), event.occurredAt()));
        }
    }

    private void log(String operationType, String businessNo, Long operatorId, String idempotencyKey) {
        mapper.insertOperationLog(new CancellationMapper.OperationLogRow(operationType, businessNo, operatorId,
                idempotencyKey));
    }

    private record FulfillmentMapperView(com.chaobo.scm.oms.infrastructure.persistence.FulfillmentMapper.FulfillmentRow row) {
        int status() { return row.status(); }
        String salesOrderNo() { return row.salesOrderNo(); }
        String outboundNo() { return row.outboundNo(); }
        String reservationRefNo() { return row.reservationRefNo(); }
    }

    public record CreateCommand(String fulfillmentNo, String reason, Long operatorId, String idempotencyKey) {}
    public record ApproveCommand(String remark, Long operatorId, String idempotencyKey) {}
    public record ProcessCommand(Long operatorId, String idempotencyKey) {}
    public record CancellationEvent(String eventId, String eventType, String businessNo, String outboundNo,
                                    String reservationRefNo, String payload) {}
}
