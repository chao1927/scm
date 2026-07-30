package com.chaobo.scm.iam.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SessionTokenPolicyTest {

    @Test
    void distinguishesAllowedRefreshReplayAndRevokedFamily() {
        assertThat(SessionTokenPolicy.decideRefresh(true, "current", "current"))
                .isEqualTo(SessionTokenPolicy.RefreshDecision.ALLOWED);
        assertThat(SessionTokenPolicy.decideRefresh(true, "current", "old"))
                .isEqualTo(SessionTokenPolicy.RefreshDecision.REPLAY);
        assertThat(SessionTokenPolicy.decideRefresh(false, "current", "current"))
                .isEqualTo(SessionTokenPolicy.RefreshDecision.REVOKED);
    }
}
