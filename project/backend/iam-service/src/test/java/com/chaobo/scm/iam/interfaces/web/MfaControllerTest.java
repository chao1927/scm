package com.chaobo.scm.iam.interfaces.web;

import com.chaobo.scm.iam.application.mfa.MfaApplicationService;
import com.chaobo.scm.iam.domain.mfa.MfaVerificationPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class MfaControllerTest {

    @Test
    void createRequiresIdempotencyHeaderAndNeverReturnsSecret() {
        StubService service = new StubService();
        MfaController controller = new MfaController(service);
        var response = controller.create("idem", new MfaController.CreateRequest(10, "IAM", "SECRET"),
                new MockHttpServletRequest());
        assertThat(response.data().challengeNo()).isEqualTo("MFA-1");
        assertThat(response.data().toString()).doesNotContain("SECRET");
    }

    private static final class StubService extends MfaApplicationService {
        private StubService() { super(null, new NoopProtector(), (secret, code, now) -> true); }
        @Override
        public ChallengeView create(CreateCommand command) {
            return new ChallengeView("MFA-1", command.userId(), command.appCode(), "TOTP", "PENDING",
                    0, 5, java.time.Instant.now().plusSeconds(60), null, 0, false);
        }
    }

    private static final class NoopProtector implements MfaVerificationPolicy.SecretProtector {
        public String encrypt(String plaintext) { return plaintext; }
        public String decrypt(String ciphertext) { return ciphertext; }
    }
}
