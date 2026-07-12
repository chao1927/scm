package com.chaobo.scm.wms.domain.inbound;

import com.chaobo.scm.common.error.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InboundOrderAggregateTest {
    @Test
    void pendingInboundCanBeCancelledWithReason() {
        var order = InboundOrderAggregate.create(1, "WIB001", "PURCHASE", "ASN001", 1001, null);

        order.cancel("采购订单取消");

        assertThat(order.status()).isEqualTo(InboundOrderStatus.CANCELLED);
        assertThat(order.version()).isEqualTo(1);
    }

    @Test
    void cancellationRequiresReason() {
        var order = InboundOrderAggregate.create(1, "WIB001", "PURCHASE", "ASN001", 1001, null);

        assertThatThrownBy(() -> order.cancel(""))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("取消原因不能为空");
    }
}
