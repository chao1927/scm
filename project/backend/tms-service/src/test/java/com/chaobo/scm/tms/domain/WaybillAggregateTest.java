package com.chaobo.scm.tms.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WaybillAggregateTest {
    @Test
    void createAndVoidWaybill() {
        WaybillAggregate waybill = WaybillAggregate.create("WB1", "TMS1", "SF", "顺丰",
                "SF123", "SF-EXPRESS", "ok");

        waybill.voidWaybill("客户取消", "APR1", 1);

        assertThat(waybill.status()).isEqualTo(WaybillAggregate.VOIDED);
        assertThat(waybill.version()).isEqualTo(2);
        assertThat(waybill.pullEvents()).extracting(TmsEvent::eventType)
                .containsExactly("WaybillCreated", "WaybillVoided");
    }

    @Test
    void rejectVoidWithWrongVersion() {
        WaybillAggregate waybill = WaybillAggregate.create("WB1", "TMS1", "SF", "顺丰",
                "SF123", "SF-EXPRESS", "ok");

        assertThatThrownBy(() -> waybill.voidWaybill("客户取消", "APR1", 9))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("version conflict");
    }
}
