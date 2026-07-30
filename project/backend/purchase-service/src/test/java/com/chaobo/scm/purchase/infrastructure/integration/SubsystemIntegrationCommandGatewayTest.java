package com.chaobo.scm.purchase.infrastructure.integration;

import com.chaobo.scm.purchase.infrastructure.persistence.integration.IntegrationCommandMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SubsystemIntegrationCommandGatewayTest。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class SubsystemIntegrationCommandGatewayTest {

    /**
     * 处理当前类型职责中的操作 {@code missingCommandRouteFailsClosedAndOpensTargetCircuit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void missingCommandRouteFailsClosedAndOpensTargetCircuit() {
        SubsystemIntegrationCommandGateway gateway = new SubsystemIntegrationCommandGateway(new MockEnvironment(), "", 100, 100, 1, 30_000);
        var command = new IntegrationCommandMapper.CommandRow(1, "CreateInboundOrder", "WMS", "PURCHASE_ORDER", "1", "PO-1", "{}", 2, 0);
        assertThatThrownBy(() -> gateway.dispatch(command)).isInstanceOf(IllegalStateException.class).hasMessageContaining("未配置跨子系统命令路由");
        assertThatThrownBy(() -> gateway.dispatch(command)).isInstanceOf(IllegalStateException.class).hasMessageContaining("熔断");
    }
}
