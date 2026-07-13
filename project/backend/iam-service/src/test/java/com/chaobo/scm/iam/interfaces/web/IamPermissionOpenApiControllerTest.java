package com.chaobo.scm.iam.interfaces.web;

import com.chaobo.scm.iam.application.IamPermissionOpenApiApplicationService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IamPermissionOpenApiControllerTest {
    @Test
    void delegatesPermissionOpenApiCommands() {
        StubPermissionService service = new StubPermissionService();
        IamPermissionOpenApiController controller = new IamPermissionOpenApiController(service);
        IamPermissionOpenApiApplicationService.TokenValidationCommand command =
                new IamPermissionOpenApiApplicationService.TokenValidationCommand("token");

        IamPermissionOpenApiApplicationService.TokenValidationResult result = controller.validate(command);

        assertThat(result.valid()).isTrue();
        assertThat(service.lastTokenCommand).isEqualTo(command);
    }

    static class StubPermissionService extends IamPermissionOpenApiApplicationService {
        IamPermissionOpenApiApplicationService.TokenValidationCommand lastTokenCommand;

        StubPermissionService() {
            super(null, null);
        }

        @Override
        public IamPermissionOpenApiApplicationService.TokenValidationResult validateToken(
                IamPermissionOpenApiApplicationService.TokenValidationCommand command) {
            lastTokenCommand = command;
            return new IamPermissionOpenApiApplicationService.TokenValidationResult(true, 1001L,
                    "admin", "IAM", 1);
        }
    }
}
