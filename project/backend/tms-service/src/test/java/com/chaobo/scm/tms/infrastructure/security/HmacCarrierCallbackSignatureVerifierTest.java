package com.chaobo.scm.tms.infrastructure.security;

import com.chaobo.scm.tms.application.CarrierCallbackSignatureVerifier;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HmacCarrierCallbackSignatureVerifierTest {

    @Test
    void verifiesRawBodyAndRejectsExpiredOrChangedPayload() throws Exception {
        long timestamp = 1_700_000_000L;
        String body = "{\"eventId\":\"evt-1\"}";
        String secret = "carrier-secret";
        MockEnvironment environment = new MockEnvironment()
            .withProperty("scm.tms.carrier-callback.secrets.SF", secret)
            .withProperty("scm.tms.carrier-callback.max-clock-skew-seconds", "300");
        var verifier = new HmacCarrierCallbackSignatureVerifier(
            environment, Clock.fixed(Instant.ofEpochSecond(timestamp), ZoneOffset.UTC));
        String signature = signature(
            secret, "SF\n" + timestamp + "\nnonce-1\n" + body);

        assertThatNoException().isThrownBy(() -> verifier.verify(
            new CarrierCallbackSignatureVerifier.SignatureInput(
                "SF", timestamp, "nonce-1", body, signature)));
        assertThatThrownBy(() -> verifier.verify(
            new CarrierCallbackSignatureVerifier.SignatureInput(
                "SF", timestamp, "nonce-1", body + " ", signature)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("signature is invalid");
        assertThatThrownBy(() -> verifier.verify(
            new CarrierCallbackSignatureVerifier.SignatureInput(
                "SF", timestamp - 301, "nonce-1", body, signature)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("timestamp expired");
    }

    private static String signature(String secret, String canonical) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(
            mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
    }
}
