package com.chaobo.scm.oms.application;

import com.chaobo.scm.oms.domain.FulfillmentAggregate;
import com.chaobo.scm.oms.infrastructure.persistence.FulfillmentMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * RocketMQ 消费应用服务到既有 Inbox 的组合契约测试。
 */
class OmsExternalEventConsumerApplicationServiceTest {

    @Test
    void routesValidInventoryEventThroughExistingInboxIdempotency() {
        var fulfillmentMapper =
                new FulfillmentApplicationServiceTest.MemoryFulfillmentMapper();
        fulfillmentMapper.fulfillments.put("FUL-1",
                new FulfillmentMapper.FulfillmentRow(
                        "FUL-1", "SO-1", "TMALL", 88L, 100L, "WH-1",
                        "STANDARD", "SKU-1:2:0:0",
                        FulfillmentAggregate.PENDING_RESERVATION,
                        "REF-1", null, null, null, null, 2));
        fulfillmentMapper.reservations.put("REF-1",
                new FulfillmentMapper.ReservationRow(
                        "REF-1", "FUL-1", null, new BigDecimal("2"),
                        BigDecimal.ZERO, 1, null, 1));
        var fulfillment = new FulfillmentApplicationService(
                fulfillmentMapper,
                new FulfillmentApplicationServiceTest.MemoryOmsMapper());
        var service = service(fulfillment);
        var event = new OmsExternalEvent(
                "INVENTORY", "INV-E1", "StockReserved", "SO-1",
                "FUL-1", "REF-1", "INV-1", new BigDecimal("2"),
                null, null, null, null, null, null, null, false, "{}");

        service.consume(event);
        service.consume(event);

        assertThat(fulfillmentMapper.inbox).hasSize(1);
        assertThat(fulfillmentMapper.reservations.get("REF-1").status())
                .isEqualTo(2);
    }

    @Test
    void rejectsSpoofedSourceBeforeInboxMutation() {
        var fulfillmentMapper =
                new FulfillmentApplicationServiceTest.MemoryFulfillmentMapper();
        var service = service(new FulfillmentApplicationService(
                fulfillmentMapper,
                new FulfillmentApplicationServiceTest.MemoryOmsMapper()));
        var event = new OmsExternalEvent(
                "WMS", "FAKE-E1", "StockReserved", "SO-1",
                "FUL-1", "REF-1", "INV-1", BigDecimal.ONE,
                null, null, null, null, null, null, null, false, "{}");

        assertThatThrownBy(() -> service.consume(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("来源");
        assertThat(fulfillmentMapper.inbox).isEmpty();
    }

    private static OmsExternalEventConsumerApplicationService service(
            FulfillmentApplicationService fulfillment) {
        var cancellationMapper =
                new CancellationAfterSaleApplicationServiceTest
                        .MemoryCancellationMapper();
        var orderMapper = new FulfillmentApplicationServiceTest.MemoryOmsMapper();
        return new OmsExternalEventConsumerApplicationService(
                fulfillment,
                new CancellationApplicationService(
                        cancellationMapper, fulfillment),
                new AfterSaleApplicationService(cancellationMapper, orderMapper),
                new ReverseAfterSaleApplicationService(
                        new ReverseAfterSaleApplicationServiceTest
                                .MemoryReverseMapper(),
                        orderMapper));
    }
}
