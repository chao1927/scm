package com.chaobo.scm.iam.domain;

import com.chaobo.scm.common.error.BusinessException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * UserAggregateTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class UserAggregateTest {

    /**
     * 处理当前类型职责中的操作 {@code userLocksAfterFiveFailedAttemptsAndCanBeEnabled}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void userLocksAfterFiveFailedAttemptsAndCanBeEnabled() {
        var user = new UserAggregate(1, "admin", "HASH:ok", 1, 0, 0);
        for (int i = 0; i < USER_LOCKS_AFTER_FIVE_FAILED_ATTEMPTS_AND_CAN_BE_ENABLED_VALUE_5; i++) {
            assertThatThrownBy(() -> user.authenticate("HASH:bad")).isInstanceOf(BusinessException.class);
        }
        assertThat(user.status()).isEqualTo(3);
        user.enable();
        user.authenticate("HASH:ok");
        assertThat(user.failedAttempts()).isZero();
    }

    /**
     * 执行命令 {@code disabledUserCannotLogin}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void disabledUserCannotLogin() {
        var user = new UserAggregate(1, "admin", "HASH:ok", 1, 0, 0);
        user.disable();
        assertThatThrownBy(() -> user.authenticate("HASH:ok")).isInstanceOf(BusinessException.class);
    }

    /**
     * 业务常量 {@code USER_LOCKS_AFTER_FIVE_FAILED_ATTEMPTS_AND_CAN_BE_ENABLED_VALUE_5}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int USER_LOCKS_AFTER_FIVE_FAILED_ATTEMPTS_AND_CAN_BE_ENABLED_VALUE_5 = 5;
}
