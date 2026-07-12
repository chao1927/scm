package com.chaobo.scm.oms.application;

import com.chaobo.scm.oms.domain.FulfillmentAggregate;
import com.chaobo.scm.oms.domain.OmsEvent;
import com.chaobo.scm.oms.domain.OutboundAggregate;
import com.chaobo.scm.oms.infrastructure.persistence.FulfillmentMapper;
import com.chaobo.scm.oms.infrastructure.persistence.OmsMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class FulfillmentApplicationService {
    private static final int RESERVATION_PENDING = 1;
    private static final int RESERVATION_RESERVED = 2;
    private static final int RESERVATION_FAILED = 3;
    private static final int RESERVATION_RELEASE_REQUESTED = 4;
    private static final int RESERVATION_RELEASED = 5;

    private final FulfillmentMapper mapper;
    private final OmsMapper omsMapper;
    private final AtomicLong fulfillmentSequence = new AtomicLong(200000);
    private final AtomicLong reservationSequence = new AtomicLong(300000);
    private final AtomicLong outboundSequence = new AtomicLong(400000);

    public FulfillmentApplicationService(FulfillmentMapper mapper, OmsMapper omsMapper) {
        this.mapper = mapper;
        this.omsMapper = omsMapper;
    }

    @Transactional
    public FulfillmentMapper.FulfillmentRow allocate(AllocateCommand command) {
        FulfillmentMapper.FulfillmentRow existing = mapper.findBySalesOrder(command.salesOrderNo());
        if (existing != null) {
            return existing;
        }
        OmsMapper.SalesOrderRow order = omsMapper.findOrder(command.salesOrderNo());
        if (order == null) {
            throw new IllegalArgumentException("sales order not found");
        }
        if (order.status() != com.chaobo.scm.oms.domain.SalesOrderAggregate.APPROVED) {
            throw new IllegalStateException("sales order is not approved");
        }
        String fulfillmentNo = "FUL" + fulfillmentSequence.incrementAndGet();
        FulfillmentAggregate aggregate = FulfillmentAggregate.create(fulfillmentNo, order.orderNo(),
                order.channelCode(), order.customerId(), command.warehouseId(), command.warehouseCode(),
                command.logisticsProductCode(), salesLines(order.linePayload()));
        FulfillmentMapper.FulfillmentRow row = toRow(aggregate);
        mapper.insertFulfillment(row);
        saveEvents(aggregate.pullEvents());
        log("ALLOCATE_FULFILLMENT", fulfillmentNo, command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public FulfillmentMapper.FulfillmentRow changeWarehouse(String fulfillmentNo, ChangeWarehouseCommand command) {
        FulfillmentAggregate aggregate = loadFulfillment(fulfillmentNo);
        aggregate.changeWarehouse(command.warehouseId(), command.warehouseCode(), command.reason());
        FulfillmentMapper.FulfillmentRow row = toRow(aggregate);
        mapper.updateFulfillment(row);
        saveEvents(aggregate.pullEvents());
        log("CHANGE_FULFILLMENT_WAREHOUSE", fulfillmentNo, command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public FulfillmentMapper.FulfillmentRow split(String fulfillmentNo, SplitCommand command) {
        FulfillmentAggregate parent = loadFulfillment(fulfillmentNo);
        String childNo = "FUL" + fulfillmentSequence.incrementAndGet();
        FulfillmentAggregate child = parent.split(childNo, command.lines(), command.reason());
        mapper.updateFulfillment(toRow(parent));
        mapper.insertFulfillment(toRow(child));
        saveEvents(parent.pullEvents());
        saveEvents(child.pullEvents());
        log("SPLIT_FULFILLMENT", fulfillmentNo, command.operatorId(), command.idempotencyKey());
        return toRow(child);
    }

    @Transactional
    public FulfillmentMapper.FulfillmentRow reserve(String fulfillmentNo, ReserveCommand command) {
        FulfillmentAggregate aggregate = loadFulfillment(fulfillmentNo);
        FulfillmentMapper.ReservationRow existing = mapper.findReservationByFulfillment(fulfillmentNo);
        if (existing != null) {
            return toRow(aggregate);
        }
        String reservationRefNo = "RESREF" + reservationSequence.incrementAndGet();
        aggregate.requestReservation(reservationRefNo);
        FulfillmentMapper.ReservationRow reservation = new FulfillmentMapper.ReservationRow(reservationRefNo,
                fulfillmentNo, null, totalQuantity(aggregate.lines()), BigDecimal.ZERO, RESERVATION_PENDING,
                null, 1);
        mapper.insertReservation(reservation);
        mapper.updateFulfillment(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        insertCommand("ReserveInventory", "INVENTORY", fulfillmentNo, reservationRefNo,
                reservationRefNo + "|" + aggregate.warehouseId() + "|" + formatLines(aggregate.lines()));
        log("REQUEST_STOCK_RESERVATION", fulfillmentNo, command.operatorId(), command.idempotencyKey());
        return toRow(aggregate);
    }

    @Transactional
    public FulfillmentMapper.FulfillmentRow releaseReservation(String reservationRefNo, ReleaseCommand command) {
        FulfillmentMapper.ReservationRow reservation = requireReservation(reservationRefNo);
        if (reservation.status() == RESERVATION_RELEASED || reservation.status() == RESERVATION_RELEASE_REQUESTED) {
            return requireFulfillment(reservation.fulfillmentNo());
        }
        if (reservation.status() != RESERVATION_RESERVED) {
            throw new IllegalStateException("reservation is not releasable");
        }
        FulfillmentMapper.ReservationRow updated = new FulfillmentMapper.ReservationRow(reservation.reservationRefNo(),
                reservation.fulfillmentNo(), reservation.reservationNo(), reservation.reserveQty(),
                reservation.reservedQty(), RESERVATION_RELEASE_REQUESTED, command.reason(), reservation.version() + 1);
        mapper.updateReservation(updated);
        insertCommand("ReleaseInventory", "INVENTORY", reservation.fulfillmentNo(),
                reservation.reservationRefNo() + ":RELEASE", reservation.reservationNo() + "|" + command.reason());
        log("REQUEST_STOCK_RELEASE", reservation.fulfillmentNo(), command.operatorId(), command.idempotencyKey());
        return requireFulfillment(reservation.fulfillmentNo());
    }

    @Transactional
    public FulfillmentMapper.FulfillmentRow createOutbound(String fulfillmentNo, CreateOutboundCommand command) {
        FulfillmentMapper.OutboundRow existing = mapper.findOutboundByFulfillment(fulfillmentNo);
        if (existing != null) {
            return requireFulfillment(fulfillmentNo);
        }
        FulfillmentAggregate fulfillment = loadFulfillment(fulfillmentNo);
        if (fulfillment.status() != FulfillmentAggregate.RESERVED) {
            throw new IllegalStateException("fulfillment is not reserved");
        }
        String outboundNo = "OUT" + outboundSequence.incrementAndGet();
        OutboundAggregate outbound = OutboundAggregate.create(outboundNo, fulfillment.fulfillmentNo(),
                fulfillment.salesOrderNo(), fulfillment.warehouseId(), fulfillment.warehouseCode());
        mapper.insertOutbound(toRow(outbound));
        saveEvents(outbound.pullEvents());
        log("CREATE_OUTBOUND", outboundNo, command.operatorId(), command.idempotencyKey());
        return requireFulfillment(fulfillmentNo);
    }

    @Transactional
    public FulfillmentMapper.OutboundRow dispatchOutbound(String outboundNo, OutboundCommand command) {
        OutboundAggregate outbound = loadOutbound(outboundNo);
        outbound.dispatch();
        FulfillmentAggregate fulfillment = loadFulfillment(outbound.fulfillmentNo());
        fulfillment.markOutboundIssued(outboundNo);
        mapper.updateOutbound(toRow(outbound));
        mapper.updateFulfillment(toRow(fulfillment));
        saveEvents(outbound.pullEvents());
        saveEvents(fulfillment.pullEvents());
        insertCommand("CreateOutboundOrder", "WMS", outboundNo, outboundNo + ":" + outbound.retryCount(),
                outbound.fulfillmentNo() + "|" + outbound.warehouseId() + "|" + outbound.warehouseCode());
        log("DISPATCH_OUTBOUND", outboundNo, command.operatorId(), command.idempotencyKey());
        return toRow(outbound);
    }

    @Transactional
    public FulfillmentMapper.OutboundRow retryOutbound(String outboundNo, OutboundCommand command) {
        OutboundAggregate outbound = loadOutbound(outboundNo);
        outbound.retryDispatch();
        mapper.updateOutbound(toRow(outbound));
        saveEvents(outbound.pullEvents());
        insertCommand("CreateOutboundOrder", "WMS", outboundNo, outboundNo + ":retry:" + outbound.retryCount(),
                outbound.fulfillmentNo() + "|" + outbound.warehouseId() + "|" + outbound.warehouseCode());
        log("RETRY_OUTBOUND", outboundNo, command.operatorId(), command.idempotencyKey());
        return toRow(outbound);
    }

    @Transactional
    public FulfillmentMapper.OutboundRow cancelOutbound(String outboundNo, CancelOutboundCommand command) {
        OutboundAggregate outbound = loadOutbound(outboundNo);
        outbound.requestCancel(command.reason());
        mapper.updateOutbound(toRow(outbound));
        saveEvents(outbound.pullEvents());
        insertCommand("CancelOutboundOrder", "WMS", outboundNo, outboundNo + ":cancel:" + outbound.version(),
                outbound.fulfillmentNo() + "|" + command.reason());
        log("CANCEL_OUTBOUND", outboundNo, command.operatorId(), command.idempotencyKey());
        return toRow(outbound);
    }

    @Transactional
    public void consumeEvent(ExternalEvent event) {
        int claimed = mapper.claimEvent(new FulfillmentMapper.EventInboxRow(event.eventId(), event.eventType(),
                event.businessNo(), event.payload(), 1, null));
        if (claimed == 0) {
            return;
        }
        try {
            switch (event.eventType()) {
                case "StockReserved" -> recordReservationSuccess(event);
                case "StockReservationFailed" -> recordReservationFailure(event);
                case "StockReleased" -> recordReservationReleased(event);
                case "WmsOutboundAccepted" -> recordWmsAccepted(event);
                case "WmsOutboundShipped" -> recordWmsShipped(event);
                case "WmsOutboundCancelled" -> recordWmsCancelled(event);
                default -> throw new IllegalArgumentException("unsupported OMS event: " + event.eventType());
            }
            mapper.updateEvent(new FulfillmentMapper.EventInboxRow(event.eventId(), event.eventType(),
                    event.businessNo(), event.payload(), 2, null));
        } catch (RuntimeException exception) {
            mapper.updateEvent(new FulfillmentMapper.EventInboxRow(event.eventId(), event.eventType(),
                    event.businessNo(), event.payload(), 3, exception.getMessage()));
            throw exception;
        }
    }

    public List<FulfillmentMapper.FulfillmentRow> listFulfillments() { return mapper.listFulfillments(); }
    public List<FulfillmentMapper.OutboundRow> listOutbounds() { return mapper.listOutbounds(); }
    public FulfillmentMapper.FulfillmentRow getFulfillment(String fulfillmentNo) { return requireFulfillment(fulfillmentNo); }
    public FulfillmentMapper.OutboundRow getOutbound(String outboundNo) { return requireOutbound(outboundNo); }

    private void recordReservationSuccess(ExternalEvent event) {
        FulfillmentMapper.ReservationRow reservation = requireReservation(event.reservationRefNo());
        FulfillmentAggregate aggregate = loadFulfillment(reservation.fulfillmentNo());
        aggregate.recordReservationSuccess(event.reservationNo(), event.quantity());
        mapper.updateFulfillment(toRow(aggregate));
        mapper.updateReservation(new FulfillmentMapper.ReservationRow(reservation.reservationRefNo(),
                reservation.fulfillmentNo(), event.reservationNo(), reservation.reserveQty(), event.quantity(),
                RESERVATION_RESERVED, null, reservation.version() + 1));
        saveEvents(aggregate.pullEvents());
    }

    private void recordReservationFailure(ExternalEvent event) {
        FulfillmentMapper.ReservationRow reservation = requireReservation(event.reservationRefNo());
        FulfillmentAggregate aggregate = loadFulfillment(reservation.fulfillmentNo());
        aggregate.recordReservationFailure(event.reason());
        mapper.updateFulfillment(toRow(aggregate));
        mapper.updateReservation(new FulfillmentMapper.ReservationRow(reservation.reservationRefNo(),
                reservation.fulfillmentNo(), reservation.reservationNo(), reservation.reserveQty(),
                reservation.reservedQty(), RESERVATION_FAILED, event.reason(), reservation.version() + 1));
        saveEvents(aggregate.pullEvents());
    }

    private void recordReservationReleased(ExternalEvent event) {
        FulfillmentMapper.ReservationRow reservation = requireReservation(event.reservationRefNo());
        mapper.updateReservation(new FulfillmentMapper.ReservationRow(reservation.reservationRefNo(),
                reservation.fulfillmentNo(), reservation.reservationNo(), reservation.reserveQty(),
                reservation.reservedQty(), RESERVATION_RELEASED, null, reservation.version() + 1));
    }

    private void recordWmsAccepted(ExternalEvent event) {
        OutboundAggregate outbound = loadOutbound(event.outboundNo());
        outbound.markWmsAccepted(event.wmsOrderNo());
        mapper.updateOutbound(toRow(outbound));
        saveEvents(outbound.pullEvents());
    }

    private void recordWmsShipped(ExternalEvent event) {
        OutboundAggregate outbound = loadOutbound(event.outboundNo());
        outbound.markShipped();
        FulfillmentAggregate fulfillment = loadFulfillment(outbound.fulfillmentNo());
        fulfillment.markWmsShipped();
        mapper.updateOutbound(toRow(outbound));
        mapper.updateFulfillment(toRow(fulfillment));
        saveEvents(outbound.pullEvents());
        saveEvents(fulfillment.pullEvents());
    }

    private void recordWmsCancelled(ExternalEvent event) {
        OutboundAggregate outbound = loadOutbound(event.outboundNo());
        outbound.markCancelled();
        FulfillmentAggregate fulfillment = loadFulfillment(outbound.fulfillmentNo());
        fulfillment.markCancelled("WMS 出库已取消");
        mapper.updateOutbound(toRow(outbound));
        mapper.updateFulfillment(toRow(fulfillment));
        saveEvents(outbound.pullEvents());
        saveEvents(fulfillment.pullEvents());
        FulfillmentMapper.ReservationRow reservation = mapper.findReservationByFulfillment(fulfillment.fulfillmentNo());
        if (reservation != null && reservation.status() == RESERVATION_RESERVED) {
            FulfillmentMapper.ReservationRow releaseRequested = new FulfillmentMapper.ReservationRow(
                    reservation.reservationRefNo(), reservation.fulfillmentNo(), reservation.reservationNo(),
                    reservation.reserveQty(), reservation.reservedQty(), RESERVATION_RELEASE_REQUESTED,
                    "WMS 出库取消后释放库存", reservation.version() + 1);
            mapper.updateReservation(releaseRequested);
            insertCommand("ReleaseInventory", "INVENTORY", fulfillment.fulfillmentNo(),
                    reservation.reservationRefNo() + ":WMS-CANCEL", reservation.reservationNo());
        }
    }

    private FulfillmentAggregate loadFulfillment(String fulfillmentNo) {
        return fromRow(requireFulfillment(fulfillmentNo));
    }

    private OutboundAggregate loadOutbound(String outboundNo) {
        return fromRow(requireOutbound(outboundNo));
    }

    private FulfillmentMapper.FulfillmentRow requireFulfillment(String fulfillmentNo) {
        FulfillmentMapper.FulfillmentRow row = mapper.findFulfillment(fulfillmentNo);
        if (row == null) {
            throw new IllegalArgumentException("fulfillment not found");
        }
        return row;
    }

    private FulfillmentMapper.ReservationRow requireReservation(String reservationRefNo) {
        FulfillmentMapper.ReservationRow row = mapper.findReservation(reservationRefNo);
        if (row == null) {
            throw new IllegalArgumentException("reservation not found");
        }
        return row;
    }

    private FulfillmentMapper.OutboundRow requireOutbound(String outboundNo) {
        FulfillmentMapper.OutboundRow row = mapper.findOutbound(outboundNo);
        if (row == null) {
            throw new IllegalArgumentException("outbound not found");
        }
        return row;
    }

    private FulfillmentAggregate fromRow(FulfillmentMapper.FulfillmentRow row) {
        return FulfillmentAggregate.restore(row.fulfillmentNo(), row.salesOrderNo(), row.channelCode(), row.customerId(),
                row.warehouseId(), row.warehouseCode(), row.logisticsProductCode(), parseLines(row.linePayload()),
                row.status(), row.reservationRefNo(), row.reservationNo(), row.outboundNo(), row.failureReason(),
                row.splitReason(), row.version());
    }

    private OutboundAggregate fromRow(FulfillmentMapper.OutboundRow row) {
        return OutboundAggregate.restore(row.outboundNo(), row.fulfillmentNo(), row.salesOrderNo(), row.warehouseId(),
                row.warehouseCode(), row.wmsOrderNo(), row.status(), row.cancelReason(), row.retryCount(), row.version());
    }

    private FulfillmentMapper.FulfillmentRow toRow(FulfillmentAggregate aggregate) {
        return new FulfillmentMapper.FulfillmentRow(aggregate.fulfillmentNo(), aggregate.salesOrderNo(),
                aggregate.channelCode(), aggregate.customerId(), aggregate.warehouseId(), aggregate.warehouseCode(),
                aggregate.logisticsProductCode(), formatLines(aggregate.lines()), aggregate.status(),
                aggregate.reservationRefNo(), aggregate.reservationNo(), aggregate.outboundNo(),
                aggregate.failureReason(), aggregate.splitReason(), aggregate.version());
    }

    private FulfillmentMapper.OutboundRow toRow(OutboundAggregate aggregate) {
        return new FulfillmentMapper.OutboundRow(aggregate.outboundNo(), aggregate.fulfillmentNo(),
                aggregate.salesOrderNo(), aggregate.warehouseId(), aggregate.warehouseCode(), aggregate.wmsOrderNo(),
                aggregate.status(), aggregate.cancelReason(), aggregate.retryCount(), aggregate.version());
    }

    private void saveEvents(List<OmsEvent> events) {
        for (OmsEvent event : events) {
            mapper.insertOutbox(new FulfillmentMapper.OutboxRow(event.eventType(), event.businessNo(),
                    event.payload(), event.occurredAt()));
        }
    }

    private void insertCommand(String commandType, String targetSystem, String businessNo, String idempotencyKey,
                               String payload) {
        mapper.insertIntegrationCommand(new FulfillmentMapper.IntegrationCommandRow(commandType, targetSystem,
                businessNo, idempotencyKey, payload));
    }

    private void log(String operationType, String businessNo, Long operatorId, String idempotencyKey) {
        mapper.insertOperationLog(new FulfillmentMapper.OperationLogRow(operationType, businessNo, operatorId,
                idempotencyKey));
    }

    private static List<FulfillmentAggregate.Line> salesLines(String payload) {
        return OmsApplicationService.parseLines(payload).stream()
                .map(line -> new FulfillmentAggregate.Line(line.skuCode(), BigDecimal.valueOf(line.quantity())))
                .toList();
    }

    private static List<FulfillmentAggregate.Line> parseLines(String payload) {
        if (payload == null || payload.isBlank()) {
            return List.of();
        }
        return List.of(payload.split(";")).stream().map(item -> {
            String[] parts = item.split(":");
            if (parts.length != 4) {
                throw new IllegalArgumentException("invalid fulfillment line payload");
            }
            return new FulfillmentAggregate.Line(parts[0], new BigDecimal(parts[1]), new BigDecimal(parts[2]),
                    new BigDecimal(parts[3]));
        }).toList();
    }

    private static String formatLines(List<FulfillmentAggregate.Line> lines) {
        return lines.stream().map(line -> String.join(":", line.skuCode(), line.quantity().toPlainString(),
                        line.reservedQty().toPlainString(), line.shippedQty().toPlainString()))
                .collect(Collectors.joining(";"));
    }

    private static BigDecimal totalQuantity(List<FulfillmentAggregate.Line> lines) {
        return lines.stream().map(FulfillmentAggregate.Line::quantity).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public record AllocateCommand(String salesOrderNo, Long warehouseId, String warehouseCode,
                                  String logisticsProductCode, Long operatorId, String idempotencyKey) {}

    public record ChangeWarehouseCommand(Long warehouseId, String warehouseCode, String reason,
                                         Long operatorId, String idempotencyKey) {}

    public record SplitCommand(List<FulfillmentAggregate.Line> lines, String reason,
                               Long operatorId, String idempotencyKey) {}

    public record ReserveCommand(Long operatorId, String idempotencyKey) {}

    public record ReleaseCommand(String reason, Long operatorId, String idempotencyKey) {}

    public record CreateOutboundCommand(Long operatorId, String idempotencyKey) {}

    public record OutboundCommand(Long operatorId, String idempotencyKey) {}

    public record CancelOutboundCommand(String reason, Long operatorId, String idempotencyKey) {}

    public record ExternalEvent(String eventId, String eventType, String businessNo, String fulfillmentNo,
                                String reservationRefNo, String reservationNo, BigDecimal quantity,
                                String outboundNo, String wmsOrderNo, String reason, String payload) {}
}
