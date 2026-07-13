package com.chaobo.scm.tms.interfaces.web;

import com.chaobo.scm.tms.application.LogisticsSettlementApplicationServiceTest;
import com.chaobo.scm.tms.domain.LogisticsExceptionAggregate;
import com.chaobo.scm.tms.domain.LogisticsFeeSourceAggregate;
import com.chaobo.scm.tms.infrastructure.persistence.LogisticsSettlementMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class LogisticsSettlementControllerTest {
    @Test
    void exceptionAndFeeSourceWorkThroughControllers() {
        LogisticsSettlementApplicationServiceTest.Services services =
                LogisticsSettlementApplicationServiceTest.servicesWithWaybill();
        LogisticsExceptionController exceptionController =
                new LogisticsExceptionController(services.exceptionService());
        LogisticsFeeSourceController feeSourceController =
                new LogisticsFeeSourceController(services.feeSourceService());

        LogisticsSettlementMapper.ExceptionRow exception = exceptionController.register(
                new LogisticsExceptionController.RegisterExceptionRequest("WB800001", "DAMAGED", "P1",
                        "外包装破损", "CARRIER", 1001L, "idem-exc"));
        LogisticsSettlementMapper.ExceptionRow closed = exceptionController.close(exception.exceptionNo(),
                new LogisticsExceptionController.CloseExceptionRequest("已索赔", "CARRIER", exception.version(),
                        1001L, "idem-close"));
        LogisticsSettlementMapper.FeeSourceRow feeSource = feeSourceController.generate("WB800001",
                new LogisticsFeeSourceController.GenerateFeeSourceRequest("FREIGHT", new BigDecimal("12.30"),
                        "CNY", "202607", "SHIPPER", 1001L, "idem-fee"));
        LogisticsSettlementMapper.FeeSourceRow pushed = feeSourceController.pushBms(feeSource.feeSourceNo(),
                new LogisticsFeeSourceController.PushBmsRequest("BMS1", 1001L, "idem-push"));

        assertThat(closed.status()).isEqualTo(LogisticsExceptionAggregate.CLOSED);
        assertThat(pushed.pushStatus()).isEqualTo(LogisticsFeeSourceAggregate.PUSHED);
        assertThat(exceptionController.list()).hasSize(1);
        assertThat(feeSourceController.list()).hasSize(1);
    }
}
