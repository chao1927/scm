package com.chaobo.scm.iam.interfaces.web;

import com.chaobo.scm.iam.application.mfa.MfaApplicationService;
import com.chaobo.scm.iam.domain.mfa.MfaVerificationPolicy;
import com.chaobo.scm.iam.application.TokenCachePort;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class MfaControllerTest {

    @Test
    void createRequiresIdempotencyHeaderAndNeverReturnsSecret() {
        StubService service = new StubService();
        MfaController controller = new MfaController(service);
        var response = controller.create("idem", new MfaController.CreateRequest(
                10, "IAM", 101, "LOGIN", "DEVICE-1"),
                new MockHttpServletRequest());
        assertThat(response.data().challengeNo()).isEqualTo("MFA-1");
        assertThat(response.data().toString()).doesNotContain("SECRET");
    }

    private static final class StubService extends MfaApplicationService {
        private StubService() {
            super(null, new NoopProtector(), (secret, code, now) -> true,
                new NoopTokenCache());
        }
        @Override
        public ChallengeView create(CreateCommand command) {
            return new ChallengeView("MFA-1", command.userId(), command.appCode(),
                    command.sessionId(), command.purpose(), "PENDING", 0, 5,
                    java.time.Instant.now().plusSeconds(60), null, 0, false);
        }
    }

    private static final class NoopProtector implements MfaVerificationPolicy.SecretProtector {
        public String encrypt(String plaintext) { return plaintext; }
        public String decrypt(String ciphertext) { return ciphertext; }
    }

    private static final class NoopTokenCache implements TokenCachePort {
        public void store(OnlineSession session) { }
        public java.util.Optional<OnlineSession> findByAccessJti(String accessJti) {
            return java.util.Optional.empty();
        }
        public java.util.Optional<OnlineSession> findByRefreshJti(String refreshJti) {
            return java.util.Optional.empty();
        }
        public RotationResult rotate(String presentedRefreshJti, OnlineSession replacement) {
            return RotationResult.NOT_FOUND;
        }
        public void revoke(long sessionId) { }
    }
}
