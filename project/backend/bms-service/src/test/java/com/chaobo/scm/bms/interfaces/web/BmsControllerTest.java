package com.chaobo.scm.bms.interfaces.web;

import com.chaobo.scm.bms.application.BmsApplicationService;
import com.chaobo.scm.bms.infrastructure.persistence.BmsMapper;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * BmsControllerTest。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class BmsControllerTest {

    /**
     * 处理当前类型职责中的操作 {@code delegatesBillingObjectAndRuleEndpoints}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void delegatesBillingObjectAndRuleEndpoints() {
        StubBmsService service = new StubBmsService();
        BmsController controller = new BmsController(service);
        BmsApplicationService.CreateBillingObjectCommand objectCommand = new BmsApplicationService.CreateBillingObjectCommand("BO1", "承运商A", "CARRIER", "PAYABLE", "CNY", 1001L, "bo-1");
        BmsApplicationService.CreateBillingRuleCommand ruleCommand = new BmsApplicationService.CreateBillingRuleCommand("BO1", "FREIGHT", BigDecimal.TEN, BigDecimal.ZERO, LocalDate.parse("2026-01-01"), LocalDate.parse("2026-12-31"), 1001L, "rule-1");
        BmsMapper.BillingObjectRow object = controller.createBillingObject(objectCommand);
        BmsMapper.BillingRuleRow rule = controller.createBillingRule(ruleCommand);
        assertThat(object.objectCode()).isEqualTo("BO1");
        assertThat(rule.ruleNo()).isEqualTo("BR1");
        assertThat(service.lastObjectCommand).isEqualTo(objectCommand);
        assertThat(service.lastRuleCommand).isEqualTo(ruleCommand);
    }

    @Test
    void delegatesHighRiskRefundResolutionEndpoint() {
        StubBmsService service = new StubBmsService();
        BmsController controller = new BmsController(service);
        var command = new BmsApplicationService.ManualRefundResolutionCommand(
            "verified unpaid", "ticket-1", 2, 1001L, 1002L, "manual-1");

        var result = controller.closeRefundManually("RF-1", command);

        assertThat(result.status()).isEqualTo(5);
        assertThat(service.lastManualCommand).isEqualTo(command);
    }

    /**
     * StubBmsService。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    static class StubBmsService extends BmsApplicationService {

        /**
         * lastObjectCommand（类型：{@code BmsApplicationService.CreateBillingObjectCommand}）。
         *
         * <p>保存当前对象所需的用例输入命令；其具体生命周期由所属对象统一管理。
         */
        BmsApplicationService.CreateBillingObjectCommand lastObjectCommand;

        /**
         * lastRuleCommand（类型：{@code BmsApplicationService.CreateBillingRuleCommand}）。
         *
         * <p>保存当前对象所需的用例输入命令；其具体生命周期由所属对象统一管理。
         */
        BmsApplicationService.CreateBillingRuleCommand lastRuleCommand;

        BmsApplicationService.ManualRefundResolutionCommand lastManualCommand;

        /**
         * 创建 StubBmsService。
         *
         * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
         */
        StubBmsService() {
            super(null);
        }

        /**
         * 执行命令 {@code createBillingObject}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param command 用例输入命令，类型为 {@code BmsApplicationService.CreateBillingObjectCommand}
         * @return 执行命令的结果，类型为 {@code BmsMapper.BillingObjectRow}
         */
        @Override
        public BmsMapper.BillingObjectRow createBillingObject(BmsApplicationService.CreateBillingObjectCommand command) {
            lastObjectCommand = command;
            return new BmsMapper.BillingObjectRow(null, command.objectCode(), command.objectName(), command.objectType(), command.direction(), command.currency(), 1, 1);
        }

        /**
         * 执行命令 {@code createBillingRule}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param command 用例输入命令，类型为 {@code BmsApplicationService.CreateBillingRuleCommand}
         * @return 执行命令的结果，类型为 {@code BmsMapper.BillingRuleRow}
         */
        @Override
        public BmsMapper.BillingRuleRow createBillingRule(BmsApplicationService.CreateBillingRuleCommand command) {
            lastRuleCommand = command;
            return new BmsMapper.BillingRuleRow(null, "BR1", command.objectCode(), command.feeType(), command.unitPrice(), command.taxRate(), command.effectiveFrom(), command.effectiveTo(), 1, 0, 1);
        }

        /**
         * 查询并返回 {@code listBillingObjects}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param status 生命周期状态，类型为 {@code Integer}
         * @return 查询并返回的结果，类型为 {@code List<BmsMapper.BillingObjectRow>}
         */
        @Override
        public List<BmsMapper.BillingObjectRow> listBillingObjects(Integer status) {
            return List.of();
        }

        @Override
        public BmsMapper.RefundSettlementRow closeRefundManually(
                String refundNo,
                BmsApplicationService.ManualRefundResolutionCommand command) {
            lastManualCommand = command;
            return new BmsMapper.RefundSettlementRow(
                1L, refundNo, "B-1", null, null, BigDecimal.TEN, "CNY", "OBJ",
                "request:" + refundNo, "digest:" + refundNo, 1,
                5, command.reason(), command.evidenceRef(), command.reviewerId(),
                command.expectedVersion() + 1);
        }
    }
}
