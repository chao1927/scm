package com.chaobo.scm.common.security;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ScmSecurityPropertiesTest。
 *
 * <p>位于公共/base 模块，仅提供稳定的跨模块类型和技术约定，不拥有任何子系统业务状态。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class ScmSecurityPropertiesTest {

    /**
     * 处理当前类型职责中的操作 {@code defaultsToEnabledAndRejectsWeakHmacSecret}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void defaultsToEnabledAndRejectsWeakHmacSecret() {
        var properties = new ScmSecurityProperties();
        assertThat(properties.isEnabled()).isTrue();
        assertThatThrownBy(() -> properties.secretKey()).isInstanceOf(IllegalStateException.class).hasMessageContaining("at least 32 bytes");
        properties.setHmacSecret("01234567890123456789012345678901");
        assertThat(properties.secretKey().getEncoded()).hasSize(32);
    }

    @Test
    void exposesActiveAndPreviousKeysForKidBasedRotation() {
        var properties = new ScmSecurityProperties();
        properties.setActiveKid("2026-07");
        properties.setHmacSecret("01234567890123456789012345678901");
        properties.setPreviousKid("2026-06");
        properties.setPreviousHmacSecret("abcdefghijklmnopqrstuvwxyzABCDEF");
        properties.setPreviousValidUntilEpochSecond(1_800_000_000L);

        assertThat(properties.getActiveKid()).isEqualTo("2026-07");
        assertThat(properties.activeSecretKey().getEncoded()).hasSize(32);
        assertThat(properties.previousSecretKey().orElseThrow().getEncoded()).hasSize(32);
        assertThat(properties.getPreviousValidUntilEpochSecond()).isEqualTo(1_800_000_000L);
    }
}
