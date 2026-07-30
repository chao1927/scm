package com.chaobo.scm.iam.domain.mfa;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class MfaChallengeAggregateTest {

    @Test
    void locksAfterMaximumFailuresAndRejectsExpiredOrReplayedChallenge() {
        Instant now = Instant.parse("2026-07-30T00:00:00Z");
        MfaChallengeAggregate challenge = MfaChallengeAggregate.create(
                1, "MFA-1", 10, "IAM", "cipher", "idem", 3, now.plusSeconds(60));

        challenge.recordFailure(now);
        challenge.recordFailure(now);
        challenge.recordFailure(now);

        assertThat(challenge.status()).isEqualTo(MfaChallengeAggregate.Status.LOCKED);
        assertThat(challenge.canVerify(now)).isFalse();

        MfaChallengeAggregate verified = MfaChallengeAggregate.create(
                2, "MFA-2", 10, "IAM", "cipher", "idem-2", 3, now.plusSeconds(60));
        verified.verify(now);
        assertThat(verified.canVerify(now.plusSeconds(1))).isFalse();

        MfaChallengeAggregate expired = MfaChallengeAggregate.create(
                3, "MFA-3", 10, "IAM", "cipher", "idem-3", 3, now.minusSeconds(1));
        assertThat(expired.canVerify(now)).isFalse();
        assertThat(expired.status()).isEqualTo(MfaChallengeAggregate.Status.EXPIRED);
    }
}
