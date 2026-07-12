package com.chaobo.scm.oms.application;

import com.chaobo.scm.oms.domain.SalesOrderAggregate;
import com.chaobo.scm.oms.infrastructure.persistence.FulfillmentMapper;
import com.chaobo.scm.oms.infrastructure.persistence.OmsMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FulfillmentApplicationServiceTest {
    @Test
    void approvedOrderCanReserveAndDispatchOutbound() {
        MemoryFulfillmentMapper mapper = new MemoryFulfillmentMapper();
        MemoryOmsMapper omsMapper = new MemoryOmsMapper();
        omsMapper.orders.put("SO-1", new OmsMapper.SalesOrderRow(1L, "SO-1", "TMALL", "C-1", 88L,
                "上海市", "SKU-1:2:10.00", new BigDecimal("20.00"), SalesOrderAggregate.APPROVED, "通过", 2));
        FulfillmentApplicationService service = new FulfillmentApplicationService(mapper, omsMapper);

        FulfillmentMapper.FulfillmentRow fulfillment = service.allocate(
                new FulfillmentApplicationService.AllocateCommand("SO-1", 100L, "WH-1", "STANDARD", 1L, "a-1"));
        service.reserve(fulfillment.fulfillmentNo(), new FulfillmentApplicationService.ReserveCommand(1L, "r-1"));
        String reservationRefNo = mapper.reservations.values().iterator().next().reservationRefNo();
        service.consumeEvent(new FulfillmentApplicationService.ExternalEvent("evt-1", "StockReserved", "RES-1",
                fulfillment.fulfillmentNo(), reservationRefNo, "INV-1", new BigDecimal("2"), null, null, null, "{}"));
        service.createOutbound(fulfillment.fulfillmentNo(),
                new FulfillmentApplicationService.CreateOutboundCommand(1L, "o-1"));
        String outboundNo = mapper.outbounds.values().iterator().next().outboundNo();
        service.dispatchOutbound(outboundNo, new FulfillmentApplicationService.OutboundCommand(1L, "d-1"));
        service.consumeEvent(new FulfillmentApplicationService.ExternalEvent("evt-2", "WmsOutboundAccepted", outboundNo,
                fulfillment.fulfillmentNo(), null, null, null, outboundNo, "WMS-1", null, "{}"));
        service.consumeEvent(new FulfillmentApplicationService.ExternalEvent("evt-3", "WmsOutboundShipped", outboundNo,
                fulfillment.fulfillmentNo(), null, null, null, outboundNo, "WMS-1", null, "{}"));
        service.consumeEvent(new FulfillmentApplicationService.ExternalEvent("evt-3", "WmsOutboundShipped", outboundNo,
                fulfillment.fulfillmentNo(), null, null, null, outboundNo, "WMS-1", null, "{}"));

        assertThat(mapper.fulfillments.get(fulfillment.fulfillmentNo()).status())
                .isEqualTo(com.chaobo.scm.oms.domain.FulfillmentAggregate.SHIPPED);
        assertThat(mapper.outbounds.get(outboundNo).status())
                .isEqualTo(com.chaobo.scm.oms.domain.OutboundAggregate.SHIPPED);
        assertThat(mapper.commands).extracting(FulfillmentMapper.IntegrationCommandRow::commandType)
                .containsExactly("ReserveInventory", "CreateOutboundOrder");
        assertThat(mapper.inbox).hasSize(3);
    }

    static class MemoryOmsMapper implements OmsMapper {
        final Map<String, SalesOrderRow> orders = new LinkedHashMap<>();

        @Override public SalesOrderRow findOrder(String orderNo) { return orders.get(orderNo); }
        @Override public SalesOrderRow findByChannelOrder(String channelCode, String channelOrderNo) { return null; }
        @Override public List<SalesOrderRow> listOrders() { return new ArrayList<>(orders.values()); }
        @Override public void insertOrder(SalesOrderRow row) { orders.put(row.orderNo(), row); }
        @Override public void updateOrder(SalesOrderRow row) { orders.put(row.orderNo(), row); }
        @Override public List<ChannelOrderRow> listChannelOrders() { return List.of(); }
        @Override public void insertChannelOrder(ChannelOrderRow row) {}
        @Override public void insertOutbox(OutboxRow row) {}
        @Override public List<OutboxRow> listOutbox() { return List.of(); }
        @Override public void insertOperationLog(OperationLogRow row) {}
        @Override public List<OperationLogRow> listOperationLogs() { return List.of(); }
    }

    static class MemoryFulfillmentMapper implements FulfillmentMapper {
        final Map<String, FulfillmentRow> fulfillments = new LinkedHashMap<>();
        final Map<String, ReservationRow> reservations = new LinkedHashMap<>();
        final Map<String, OutboundRow> outbounds = new LinkedHashMap<>();
        final List<IntegrationCommandRow> commands = new ArrayList<>();
        final Map<String, EventInboxRow> inbox = new LinkedHashMap<>();
        final List<OutboxRow> outbox = new ArrayList<>();

        @Override public FulfillmentRow findFulfillment(String fulfillmentNo) { return fulfillments.get(fulfillmentNo); }
        @Override public FulfillmentRow findBySalesOrder(String salesOrderNo) {
            return fulfillments.values().stream().filter(row -> row.salesOrderNo().equals(salesOrderNo)).findFirst().orElse(null);
        }
        @Override public List<FulfillmentRow> listFulfillments() { return new ArrayList<>(fulfillments.values()); }
        @Override public void insertFulfillment(FulfillmentRow row) { fulfillments.put(row.fulfillmentNo(), row); }
        @Override public void updateFulfillment(FulfillmentRow row) { fulfillments.put(row.fulfillmentNo(), row); }
        @Override public ReservationRow findReservation(String reservationRefNo) { return reservations.get(reservationRefNo); }
        @Override public ReservationRow findReservationByFulfillment(String fulfillmentNo) {
            return reservations.values().stream().filter(row -> row.fulfillmentNo().equals(fulfillmentNo)).findFirst().orElse(null);
        }
        @Override public void insertReservation(ReservationRow row) { reservations.put(row.reservationRefNo(), row); }
        @Override public void updateReservation(ReservationRow row) { reservations.put(row.reservationRefNo(), row); }
        @Override public OutboundRow findOutbound(String outboundNo) { return outbounds.get(outboundNo); }
        @Override public OutboundRow findOutboundByFulfillment(String fulfillmentNo) {
            return outbounds.values().stream().filter(row -> row.fulfillmentNo().equals(fulfillmentNo)).findFirst().orElse(null);
        }
        @Override public List<OutboundRow> listOutbounds() { return new ArrayList<>(outbounds.values()); }
        @Override public void insertOutbound(OutboundRow row) { outbounds.put(row.outboundNo(), row); }
        @Override public void updateOutbound(OutboundRow row) { outbounds.put(row.outboundNo(), row); }
        @Override public void insertIntegrationCommand(IntegrationCommandRow row) { commands.add(row); }
        @Override public int claimEvent(EventInboxRow row) { return inbox.putIfAbsent(row.eventId(), row) == null ? 1 : 0; }
        @Override public void updateEvent(EventInboxRow row) { inbox.put(row.eventId(), row); }
        @Override public void insertOutbox(OutboxRow row) { outbox.add(row); }
        @Override public void insertOperationLog(OperationLogRow row) {}
    }
}
