package com.chaobo.scm.tms.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LogisticsSettlementAggregateTest {
    @Test
    void closeExceptionRequiresVersionAndResponsibleParty() {
        LogisticsExceptionAggregate exception = LogisticsExceptionAggregate.register("EXC1", "WB1",
                "DAMAGED", "P1", "外包装破损", "CARRIER");

        exception.close("已索赔", "CARRIER", 1);

        assertThat(exception.status()).isEqualTo(LogisticsExceptionAggregate.CLOSED);
        assertThat(exception.pullEvents()).extracting(TmsEvent::eventType)
                .containsExactly("LogisticsExceptionRegistered", "LogisticsExceptionClosed");
    }

    @Test
    void rejectCloseExceptionWithoutResponsibleParty() {
        LogisticsExceptionAggregate exception = LogisticsExceptionAggregate.register("EXC1", "WB1",
                "DAMAGED", "P1", "外包装破损", null);

        assertThatThrownBy(() -> exception.close("已索赔", "", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("responsible party");
    }

    @Test
    void generateAndPushFeeSource() {
        LogisticsFeeSourceAggregate feeSource = LogisticsFeeSourceAggregate.generate("FEE1", "WB1", "SF",
                "SF-EXPRESS", "FREIGHT", new BigDecimal("12.30"), "CNY", "202607", "SHIPPER");

        feeSource.pushToBms("BMS1");

        assertThat(feeSource.pushStatus()).isEqualTo(LogisticsFeeSourceAggregate.PUSHED);
        assertThat(feeSource.pullEvents()).extracting(TmsEvent::eventType)
                .containsExactly("LogisticsFeeSourceGenerated", "LogisticsFeeSourcePushed");
    }
}
