package com.chaobo.scm.tms.interfaces.web;

import com.chaobo.scm.tms.application.LogisticsSettlementApplicationServiceTest;
import com.chaobo.scm.tms.domain.LogisticsExceptionAggregate;
import com.chaobo.scm.tms.domain.LogisticsFeeSourceAggregate;
import com.chaobo.scm.tms.infrastructure.persistence.LogisticsSettlementMapper;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * LogisticsSettlementControllerTest。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class LogisticsSettlementControllerTest {

    /**
     * 处理当前类型职责中的操作 {@code exceptionAndFeeSourceWorkThroughControllers}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void exceptionAndFeeSourceWorkThroughControllers() {
        LogisticsSettlementApplicationServiceTest.Services services = LogisticsSettlementApplicationServiceTest.servicesWithWaybill();
        LogisticsExceptionController exceptionController = new LogisticsExceptionController(services.exceptionService());
        LogisticsFeeSourceController feeSourceController = new LogisticsFeeSourceController(services.feeSourceService());
        LogisticsSettlementMapper.ExceptionRow exception = exceptionController.register(new LogisticsExceptionController.RegisterExceptionRequest("WB800001", "DAMAGED", "P1", "外包装破损", "CARRIER", 1001L, "idem-exc"));
        LogisticsSettlementMapper.ExceptionRow closed = exceptionController.close(exception.exceptionNo(), new LogisticsExceptionController.CloseExceptionRequest("已索赔", "CARRIER", exception.version(), 1001L, "idem-close"));
        LogisticsSettlementMapper.FeeSourceRow feeSource = feeSourceController.generate("WB800001", new LogisticsFeeSourceController.GenerateFeeSourceRequest("FREIGHT", new BigDecimal("12.30"), "CNY", "202607", "SHIPPER", 1001L, "idem-fee"));
        LogisticsSettlementMapper.FeeSourceRow pushed = feeSourceController.pushBms(feeSource.feeSourceNo(), new LogisticsFeeSourceController.PushBmsRequest("BMS1", 1001L, "idem-push"));
        assertThat(closed.status()).isEqualTo(LogisticsExceptionAggregate.CLOSED);
        assertThat(pushed.pushStatus()).isEqualTo(LogisticsFeeSourceAggregate.PUSHED);
        assertThat(exceptionController.list()).hasSize(1);
        assertThat(feeSourceController.list()).hasSize(1);
    }
}
