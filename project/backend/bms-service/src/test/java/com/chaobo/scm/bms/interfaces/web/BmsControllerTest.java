package com.chaobo.scm.bms.interfaces.web;

import com.chaobo.scm.bms.application.BmsApplicationService;
import com.chaobo.scm.bms.infrastructure.persistence.BmsMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BmsControllerTest {
    @Test
    void delegatesBillingObjectRuleAndChargeSourceEndpoints() {
        StubBmsService service = new StubBmsService();
        BmsController controller = new BmsController(service);
        BmsApplicationService.CreateBillingObjectCommand objectCommand =
                new BmsApplicationService.CreateBillingObjectCommand("BO1", "承运商A", "CARRIER",
                        "PAYABLE", "CNY", 1001L, "bo-1");
        BmsApplicationService.CreateBillingRuleCommand ruleCommand =
                new BmsApplicationService.CreateBillingRuleCommand("BO1", "FREIGHT", BigDecimal.TEN,
                        BigDecimal.ZERO, LocalDate.parse("2026-01-01"), LocalDate.parse("2026-12-31"),
                        1001L, "rule-1");
        BmsApplicationService.CollectChargeSourceCommand sourceCommand =
                new BmsApplicationService.CollectChargeSourceCommand("TMS", "EVT1", "src-1", "BO1",
                        "FREIGHT", BigDecimal.ONE, "2026-07", "{}", 1001L);

        BmsMapper.BillingObjectRow object = controller.createBillingObject(objectCommand);
        BmsMapper.BillingRuleRow rule = controller.createBillingRule(ruleCommand);
        BmsMapper.ChargeSourceRow source = controller.collectChargeSource(sourceCommand);

        assertThat(object.objectCode()).isEqualTo("BO1");
        assertThat(rule.ruleNo()).isEqualTo("BR1");
        assertThat(source.sourceNo()).isEqualTo("CS1");
        assertThat(service.lastObjectCommand).isEqualTo(objectCommand);
        assertThat(service.lastRuleCommand).isEqualTo(ruleCommand);
        assertThat(service.lastSourceCommand).isEqualTo(sourceCommand);
    }

    static class StubBmsService extends BmsApplicationService {
        BmsApplicationService.CreateBillingObjectCommand lastObjectCommand;
        BmsApplicationService.CreateBillingRuleCommand lastRuleCommand;
        BmsApplicationService.CollectChargeSourceCommand lastSourceCommand;

        StubBmsService() {
            super(null);
        }

        @Override
        public BmsMapper.BillingObjectRow createBillingObject(
                BmsApplicationService.CreateBillingObjectCommand command) {
            lastObjectCommand = command;
            return new BmsMapper.BillingObjectRow(null, command.objectCode(), command.objectName(),
                    command.objectType(), command.direction(), command.currency(), 1, 1);
        }

        @Override
        public BmsMapper.BillingRuleRow createBillingRule(BmsApplicationService.CreateBillingRuleCommand command) {
            lastRuleCommand = command;
            return new BmsMapper.BillingRuleRow(null, "BR1", command.objectCode(), command.feeType(),
                    command.unitPrice(), command.taxRate(), command.effectiveFrom(), command.effectiveTo(), 1, 0, 1);
        }

        @Override
        public BmsMapper.ChargeSourceRow collectChargeSource(
                BmsApplicationService.CollectChargeSourceCommand command) {
            lastSourceCommand = command;
            return new BmsMapper.ChargeSourceRow(null, "CS1", command.sourceSystem(), command.sourceEventId(),
                    command.idempotencyKey(), command.billingObjectCode(), command.feeType(), command.quantity(),
                    command.billingPeriod(), command.payload(), 2, null, 1);
        }

        @Override
        public List<BmsMapper.BillingObjectRow> listBillingObjects(Integer status) {
            return List.of();
        }
    }
}
