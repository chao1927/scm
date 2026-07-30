package com.chaobo.scm.iam.interfaces.web;

import com.chaobo.scm.iam.application.IamApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class IamControllerTest {

    @Test
    void logoutDelegatesRefreshTokenAndRemainsSafeForRepeatedRequests() {
        StubIamApplicationService service = new StubIamApplicationService();
        IamController controller = new IamController(service);
        MockHttpServletRequest request = new MockHttpServletRequest();

        controller.logout(new IamController.RefreshRequest("refresh-token"), request);
        controller.logout(new IamController.RefreshRequest("refresh-token"), request);

        assertThat(service.logoutCalls).isEqualTo(2);
        assertThat(service.lastRefreshToken).isEqualTo("refresh-token");
    }

    private static final class StubIamApplicationService extends IamApplicationService {

        private int logoutCalls;
        private String lastRefreshToken;

        private StubIamApplicationService() {
            super(null, null, null, null, null);
        }

        @Override
        public void logout(String refreshToken) {
            logoutCalls++;
            lastRefreshToken = refreshToken;
        }
    }
}
