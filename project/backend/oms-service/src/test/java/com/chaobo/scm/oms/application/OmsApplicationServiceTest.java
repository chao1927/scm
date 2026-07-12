package com.chaobo.scm.oms.application;

import com.chaobo.scm.oms.domain.SalesOrderAggregate;
import com.chaobo.scm.oms.infrastructure.persistence.OmsMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OmsApplicationServiceTest {
    @Test
    void receiveChannelOrderIsIdempotentAndCanReview() {
        MemoryOmsMapper mapper = new MemoryOmsMapper();
        OmsApplicationService service = new OmsApplicationService(mapper);
        OmsApplicationService.ReceiveChannelOrder command = new OmsApplicationService.ReceiveChannelOrder(
                "TMALL", "C1001", 88L, "上海市浦东新区",
                List.of(new SalesOrderAggregate.OrderLine("SKU1", 2, new BigDecimal("10.00"))),
                "{\"channelOrderNo\":\"C1001\"}", 1001L, "idem-1");

        OmsMapper.SalesOrderRow first = service.receiveChannelOrder(command);
        OmsMapper.SalesOrderRow second = service.receiveChannelOrder(command);
        OmsMapper.SalesOrderRow reviewed = service.reviewSalesOrder(first.orderNo(),
                new OmsApplicationService.ReviewCommand(true, "通过", 1002L, "idem-2"));

        assertThat(second.orderNo()).isEqualTo(first.orderNo());
        assertThat(reviewed.status()).isEqualTo(SalesOrderAggregate.APPROVED);
        assertThat(service.listChannelOrders()).hasSize(1);
        assertThat(service.listOutbox()).extracting(OmsMapper.OutboxRow::eventType)
                .contains("ChannelOrderReceived", "SalesOrderCreated", "SalesOrderReviewed");
        assertThat(service.listOperationLogs()).extracting(OmsMapper.OperationLogRow::operationType)
                .contains("RECEIVE_CHANNEL_ORDER", "APPROVE_SALES_ORDER");
    }

    static class MemoryOmsMapper implements OmsMapper {
        final Map<String, SalesOrderRow> orders = new LinkedHashMap<>();
        final Map<String, SalesOrderRow> channelIndex = new LinkedHashMap<>();
        final List<ChannelOrderRow> channelOrders = new ArrayList<>();
        final List<OutboxRow> outbox = new ArrayList<>();
        final List<OperationLogRow> logs = new ArrayList<>();

        @Override
        public SalesOrderRow findOrder(String orderNo) { return orders.get(orderNo); }

        @Override
        public SalesOrderRow findByChannelOrder(String channelCode, String channelOrderNo) { return channelIndex.get(channelCode + ":" + channelOrderNo); }

        @Override
        public List<SalesOrderRow> listOrders() { return new ArrayList<>(orders.values()); }

        @Override
        public void insertOrder(SalesOrderRow row) {
            orders.put(row.orderNo(), row);
            channelIndex.put(row.channelCode() + ":" + row.channelOrderNo(), row);
        }

        @Override
        public void updateOrder(SalesOrderRow row) {
            orders.put(row.orderNo(), row);
            channelIndex.put(row.channelCode() + ":" + row.channelOrderNo(), row);
        }

        @Override
        public List<ChannelOrderRow> listChannelOrders() { return channelOrders; }

        @Override
        public void insertChannelOrder(ChannelOrderRow row) { channelOrders.add(row); }

        @Override
        public void insertOutbox(OutboxRow row) { outbox.add(row); }

        @Override
        public List<OutboxRow> listOutbox() { return outbox; }

        @Override
        public void insertOperationLog(OperationLogRow row) { logs.add(row); }

        @Override
        public List<OperationLogRow> listOperationLogs() { return logs; }
    }
}
