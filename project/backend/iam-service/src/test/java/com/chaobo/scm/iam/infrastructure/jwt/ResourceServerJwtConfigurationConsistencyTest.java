package com.chaobo.scm.iam.infrastructure.jwt;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceServerJwtConfigurationConsistencyTest {

    private static final String IAM_SERVICE_DIRECTORY = "iam-service";

    private static final String BACKEND_DIRECTORY = "project/backend";

    private static final List<String> SERVICES = List.of(
            "supplier-service", "purchase-service", "wms-service", "inventory-service",
            "oms-service", "tms-service", "bms-service", "mdm-service", "iam-service");

    @Test
    void allNineServicesUseTheSameFailClosedActiveAndPreviousKeyContract() throws IOException {
        Path backend = backendRoot();

        for (String service : SERVICES) {
            String yaml = Files.readString(backend.resolve(service)
                    .resolve("src/main/resources/application.yml"));
            assertThat(yaml).as(service)
                    .contains("active-kid: ${IAM_JWT_ACTIVE_KID:active}")
                    .contains("hmac-secret: ${IAM_JWT_SECRET:}")
                    .contains("previous-kid: ${IAM_JWT_PREVIOUS_KID:}")
                    .contains("previous-hmac-secret: ${IAM_JWT_PREVIOUS_SECRET:}")
                    .contains("previous-valid-until-epoch-second: ${IAM_JWT_PREVIOUS_VALID_UNTIL_EPOCH_SECOND:0}")
                    .doesNotContain("jwk-set-uri:");
        }
    }

    private static Path backendRoot() {
        Path current = Path.of("").toAbsolutePath();
        if (Files.isDirectory(current.resolve(IAM_SERVICE_DIRECTORY))) {
            return current;
        }
        if (Files.isDirectory(current.resolve(BACKEND_DIRECTORY).resolve(IAM_SERVICE_DIRECTORY))) {
            return current.resolve(BACKEND_DIRECTORY);
        }
        Path parent = current.getParent();
        if (parent != null && Files.isDirectory(parent.resolve(IAM_SERVICE_DIRECTORY))) {
            return parent;
        }
        throw new IllegalStateException("project/backend root not found from " + current);
    }
}
