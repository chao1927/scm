package com.chaobo.scm.bms.infrastructure.integration;

import com.chaobo.scm.bms.application.integration.PaymentCallbackSignatureVerifier;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HmacPaymentCallbackSignatureVerifierTest {

    @Test
    void verifiesRawBodyAndClockWindow() throws Exception {
        long timestamp = 1_700_000_000L;
        String secret = "payment-secret";
        String body = "{\"refundNo\":\"RF-1\"}";
        String signature = sign(secret, timestamp + "\nnonce-1\n" + body);
        var verifier = new HmacPaymentCallbackSignatureVerifier(
            secret, 300, Clock.fixed(
                Instant.ofEpochSecond(timestamp), ZoneOffset.UTC));

        assertThatNoException().isThrownBy(() -> verifier.verify(
            new PaymentCallbackSignatureVerifier.SignatureInput(
                timestamp, "nonce-1", body, signature)));
        assertThatThrownBy(() -> verifier.verify(
            new PaymentCallbackSignatureVerifier.SignatureInput(
                timestamp, "nonce-1", body + " ", signature)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("signature is invalid");
    }

    private static String sign(String secret, String canonical) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(
            secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(
            mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
    }
}
