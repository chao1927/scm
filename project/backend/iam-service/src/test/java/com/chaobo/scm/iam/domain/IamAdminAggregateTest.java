package com.chaobo.scm.iam.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * IamAdminAggregateTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class IamAdminAggregateTest {

    /**
     * 处理当前类型职责中的操作 {@code applicationCanChangeAndDisableWithVersion}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void applicationCanChangeAndDisableWithVersion() {
        IamApplicationAggregate app = IamApplicationAggregate.create("OMS", "订单系统", "/oms");
        app.change("订单履约系统", "/oms/home", 1);
        app.disable(2);
        assertThat(app.status()).isEqualTo(IamApplicationAggregate.DISABLED);
        assertThat(app.version()).isEqualTo(3);
    }

    /**
     * 处理当前类型职责中的操作 {@code applicationRejectsVersionConflict}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void applicationRejectsVersionConflict() {
        IamApplicationAggregate app = IamApplicationAggregate.create("OMS", "订单系统", "/oms");
        assertThatThrownBy(() -> app.disable(2)).isInstanceOf(IllegalStateException.class).hasMessageContaining("version conflict");
    }

    /**
     * 处理当前类型职责中的操作 {@code ssoClientResetsSecretWithVersion}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void ssoClientResetsSecretWithVersion() {
        IamSsoClientAggregate client = IamSsoClientAggregate.configure("OMS-WEB", "OMS", "https://oms.example/callback", "HASH:old");
        client.resetSecret("HASH:new", 1);
        assertThat(client.secretHash()).isEqualTo("HASH:new");
        assertThat(client.version()).isEqualTo(2);
    }
}
