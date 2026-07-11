package com.chaobo.scm.purchase.domain.order;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PurchaseOrderAggregateTest {
    private final IdentifierGenerator ids = new TestIdentifierGenerator();

    @Test
    void submitApproveAndPublishChangesStatus() {
        var order = order(BigDecimal.ZERO);
        order.pullEvents();

        order.submit(ids);
        order.approve(true, null, ids);
        order.publish("EVENT", ids);

        assertThat(order.status()).isEqualTo(PurchaseOrderStatus.PENDING_SUPPLIER_CONFIRM);
        assertThat(order.releasedAt()).isNotNull();
        assertThat(order.pullEvents()).extracting("eventType")
                .contains("PurchaseOrderSubmitted", "PurchaseOrderApproved", "PurchaseOrderPublished");
    }

    @Test
    void cancelRejectsReceivedOrder() {
        var order = order(new BigDecimal("1"));

        assertThatThrownBy(() -> order.cancel("不采购了", ids))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已有入库执行");
    }

    @Test
    void applyLineQtyChangeIncrementsBusinessVersion() {
        var order = order(BigDecimal.ZERO);
        var lineId = order.lines().getFirst().lineId();

        order.applyLineQtyChanges(Map.of(lineId, new BigDecimal("20")), ids);

        assertThat(order.versionNo()).isEqualTo(2);
        assertThat(order.lines().getFirst().orderQty()).isEqualByComparingTo("20");
    }

    private PurchaseOrderAggregate order(BigDecimal receivedQty) {
        return PurchaseOrderAggregate.create(1, 3001, "SUP001", "测试供应商", 2001, "WH001", "CNY",
                List.of(new PurchaseOrderLine(ids.nextId(), "SKU-01", "测试SKU", new BigDecimal("10"),
                        new BigDecimal("12"), new BigDecimal("0.13"), null, LocalDate.now().plusDays(7),
                        receivedQty)), ids);
    }

    private static final class TestIdentifierGenerator implements IdentifierGenerator {
        private final AtomicLong sequence = new AtomicLong(5000);

        @Override
        public long nextId() {
            return sequence.incrementAndGet();
        }

        @Override
        public String nextCode(String prefix) {
            return prefix + sequence.incrementAndGet();
        }
    }
}
