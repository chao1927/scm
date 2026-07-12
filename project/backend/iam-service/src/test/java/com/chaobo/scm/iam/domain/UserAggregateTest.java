package com.chaobo.scm.iam.domain;

import com.chaobo.scm.common.error.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserAggregateTest {
    @Test
    void userLocksAfterFiveFailedAttemptsAndCanBeEnabled() {
        var user = new UserAggregate(1, "admin", "HASH:ok", 1, 0, 0);

        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(() -> user.authenticate("HASH:bad")).isInstanceOf(BusinessException.class);
        }

        assertThat(user.status()).isEqualTo(3);
        user.enable();
        user.authenticate("HASH:ok");
        assertThat(user.failedAttempts()).isZero();
    }

    @Test
    void disabledUserCannotLogin() {
        var user = new UserAggregate(1, "admin", "HASH:ok", 1, 0, 0);
        user.disable();

        assertThatThrownBy(() -> user.authenticate("HASH:ok")).isInstanceOf(BusinessException.class);
    }
}
