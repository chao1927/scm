package com.chaobo.scm.tms.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShippingLabelAggregateTest {
    @Test
    void generateAndPrintLabel() {
        ShippingLabelAggregate label = ShippingLabelAggregate.generate("LBL1", "WB1", "PKG1",
                "SF-V1", "oss://labels/LBL1.pdf");

        label.print("PRINTER-1");

        assertThat(label.status()).isEqualTo(ShippingLabelAggregate.PRINTED);
        assertThat(label.printCount()).isEqualTo(1);
        assertThat(label.pullEvents()).extracting(TmsEvent::eventType)
                .containsExactly("ShippingLabelGenerated", "ShippingLabelPrinted");
    }

    @Test
    void rejectPrintVoidedLabel() {
        ShippingLabelAggregate label = ShippingLabelAggregate.generate("LBL1", "WB1", "PKG1",
                "SF-V1", "oss://labels/LBL1.pdf");
        label.voidLabel("运单作废");

        assertThatThrownBy(() -> label.print("PRINTER-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("voided label");
    }
}
