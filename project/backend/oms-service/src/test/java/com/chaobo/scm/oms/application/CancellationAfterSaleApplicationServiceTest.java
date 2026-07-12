package com.chaobo.scm.oms.application;

import com.chaobo.scm.oms.domain.FulfillmentAggregate;
import com.chaobo.scm.oms.domain.SalesOrderAggregate;
import com.chaobo.scm.oms.infrastructure.persistence.CancellationMapper;
import com.chaobo.scm.oms.infrastructure.persistence.FulfillmentMapper;
import com.chaobo.scm.oms.infrastructure.persistence.OmsMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CancellationAfterSaleApplicationServiceTest {
    @Test
    void cancellationWithoutOutboundCompletesAfterLocalStockCheck() {
        FulfillmentApplicationServiceTest.MemoryFulfillmentMapper fulfillmentMapper =
                new FulfillmentApplicationServiceTest.MemoryFulfillmentMapper();
        fulfillmentMapper.fulfillments.put("FUL-1", new FulfillmentMapper.FulfillmentRow(
                "FUL-1", "SO-1", "TMALL", 88L, 100L, "WH-1", "STANDARD", "SKU-1:2:0:0",
                FulfillmentAggregate.PENDING_RESERVATION, null, null, null, null, null, 1));
        MemoryCancellationMapper cancellationMapper = new MemoryCancellationMapper();
        CancellationApplicationService service = new CancellationApplicationService(cancellationMapper,
                new FulfillmentApplicationService(fulfillmentMapper, new FulfillmentApplicationServiceTest.MemoryOmsMapper()));

        CancellationMapper.CancelRow created = service.create(new CancellationApplicationService.CreateCommand(
                "FUL-1", "客户取消", 1L, "c-1"));
        service.approve(created.cancellationNo(), new CancellationApplicationService.ApproveCommand("同意", 1L, "c-2"));
        CancellationMapper.CancelRow completed = service.process(created.cancellationNo(),
                new CancellationApplicationService.ProcessCommand(1L, "c-3"));

        assertThat(completed.status()).isEqualTo(4);
        assertThat(cancellationMapper.outbox).extracting(CancellationMapper.OutboxRow::eventType)
                .contains("SalesOrderCanceled");
    }

    @Test
    void approvedAfterSaleRequestsBmsRefundAndConsumesCompletion() {
        MemoryCancellationMapper cancellationMapper = new MemoryCancellationMapper();
        FulfillmentApplicationServiceTest.MemoryOmsMapper omsMapper = new FulfillmentApplicationServiceTest.MemoryOmsMapper();
        omsMapper.orders.put("SO-1", new OmsMapper.SalesOrderRow(1L, "SO-1", "TMALL", "C-1", 88L,
                "上海市", "SKU-1:2:10.00", new BigDecimal("20.00"), SalesOrderAggregate.APPROVED, "通过", 2));
        AfterSaleApplicationService service = new AfterSaleApplicationService(cancellationMapper, omsMapper);

        CancellationMapper.AfterSaleRow created = service.create(new AfterSaleApplicationService.CreateCommand(
                "SO-1", "FUL-1", new BigDecimal("20.00"), "仅退款", 1L, "a-1"));
        service.approve(created.afterSaleNo(), new AfterSaleApplicationService.ApproveCommand("同意", 1L, "a-2"));
        service.requestRefund(created.afterSaleNo(), new AfterSaleApplicationService.RefundCommand(1L, "a-3"));
        service.consumeEvent(new AfterSaleApplicationService.RefundEvent("refund-1", "RefundCompleted",
                created.afterSaleNo(), created.afterSaleNo(), new BigDecimal("20.00"), "{}"));
        CancellationMapper.AfterSaleRow completed = service.complete(created.afterSaleNo(),
                new AfterSaleApplicationService.CompleteCommand(1L, "a-4"));

        assertThat(completed.status()).isEqualTo(5);
        assertThat(cancellationMapper.commands).extracting(CancellationMapper.IntegrationCommandRow::targetSystem)
                .containsExactly("BMS");
    }

    static class MemoryCancellationMapper implements CancellationMapper {
        final Map<String, CancelRow> cancels = new LinkedHashMap<>();
        final Map<String, AfterSaleRow> afterSales = new LinkedHashMap<>();
        final List<IntegrationCommandRow> commands = new ArrayList<>();
        final Map<String, EventInboxRow> inbox = new LinkedHashMap<>();
        final List<OutboxRow> outbox = new ArrayList<>();

        @Override public CancelRow findCancel(String cancellationNo) { return cancels.get(cancellationNo); }
        @Override public CancelRow findCancelByFulfillment(String fulfillmentNo) {
            return cancels.values().stream().filter(row -> row.fulfillmentNo().equals(fulfillmentNo)).findFirst().orElse(null);
        }
        @Override public CancelRow findCancelByOutbound(String outboundNo) { return null; }
        @Override public CancelRow findCancelByReservation(String reservationRefNo) { return null; }
        @Override public void insertCancel(CancelRow row) { cancels.put(row.cancellationNo(), row); }
        @Override public void updateCancel(CancelRow row) { cancels.put(row.cancellationNo(), row); }
        @Override public AfterSaleRow findAfterSale(String afterSaleNo) { return afterSales.get(afterSaleNo); }
        @Override public AfterSaleRow findAfterSaleByOrder(String salesOrderNo) {
            return afterSales.values().stream().filter(row -> row.salesOrderNo().equals(salesOrderNo)).findFirst().orElse(null);
        }
        @Override public void insertAfterSale(AfterSaleRow row) { afterSales.put(row.afterSaleNo(), row); }
        @Override public void updateAfterSale(AfterSaleRow row) { afterSales.put(row.afterSaleNo(), row); }
        @Override public void insertIntegrationCommand(IntegrationCommandRow row) { commands.add(row); }
        @Override public int claimEvent(EventInboxRow row) { return inbox.putIfAbsent(row.eventId(), row) == null ? 1 : 0; }
        @Override public void updateEvent(EventInboxRow row) { inbox.put(row.eventId(), row); }
        @Override public void insertOutbox(OutboxRow row) { outbox.add(row); }
        @Override public void insertOperationLog(OperationLogRow row) {}
    }
}
