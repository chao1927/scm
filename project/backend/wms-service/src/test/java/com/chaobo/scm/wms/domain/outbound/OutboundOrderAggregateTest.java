package com.chaobo.scm.wms.domain.outbound;

import com.chaobo.scm.common.error.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutboundOrderAggregateTest {
    @Test
    void allocationAndCancellationFollowStateMachine() {
        var outbound = new OutboundOrderAggregate(1, "OUT1", "OMS", "SO1", 1, 1, 0);

        outbound.allocate();
        assertThat(outbound.status()).isEqualTo(2);

        outbound.cancel("客户取消");
        assertThat(outbound.status()).isEqualTo(9);
        assertThatThrownBy(outbound::allocate).isInstanceOf(BusinessException.class);
    }

    @Test
    void cancelRequiresReasonAndRejectsRepeatedCancel() {
        var outbound = new OutboundOrderAggregate(1, "OUT1", "OMS", "SO1", 1, 1, 0);

        assertThatThrownBy(() -> outbound.cancel("")).isInstanceOf(BusinessException.class);

        outbound.cancel("客户取消");
        assertThatThrownBy(() -> outbound.cancel("重复取消")).isInstanceOf(BusinessException.class);
    }
}
