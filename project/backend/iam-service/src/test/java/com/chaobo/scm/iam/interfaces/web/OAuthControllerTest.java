package com.chaobo.scm.iam.interfaces.web;

import com.chaobo.scm.iam.application.OAuthApplicationService;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OAuthControllerTest {

    @Test
    void clientCredentialsUsesStandardTokenResponseAndDoesNotExposeUserTokens() {
        OAuthController controller = new OAuthController(new StubOAuthService());

        var response = controller.token("client_credentials", "OMS-WEB", "secret",
                null, null, null, null, "inventory.read");

        assertThat(response).containsEntry("access_token", "machine-access")
                .containsEntry("token_type", "Bearer")
                .containsEntry("scope", "inventory.read")
                .doesNotContainKeys("refresh_token", "id_token");
    }

    private static final class StubOAuthService extends OAuthApplicationService {
        private StubOAuthService() { super(null, null); }

        @Override
        public TokenResponse issueClientCredentials(ClientCredentialsTokenRequest request) {
            assertThat(request.clientId()).isEqualTo("OMS-WEB");
            assertThat(request.scopes()).containsExactly("inventory.read");
            return new TokenResponse("machine-access", null, null, "Bearer", 900,
                    Set.of("inventory.read"));
        }
    }
}
