package com.chaobo.scm.bms.infrastructure.integration;

import com.chaobo.scm.bms.application.integration.PaymentCallbackSignatureVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;

/**
 * 支付回调 HMAC-SHA256 验签器。
 *
 * @author SCM Team
 */
@Component
public class HmacPaymentCallbackSignatureVerifier
        implements PaymentCallbackSignatureVerifier {

    private final String secret;
    private final long maxClockSkewSeconds;
    private final Clock clock;

    public HmacPaymentCallbackSignatureVerifier(
            @Value("${scm.bms.external.shared-secret}") String secret,
            @Value("${scm.bms.external.callback-max-clock-skew-seconds:300}")
            long maxClockSkewSeconds) {
        this(secret, maxClockSkewSeconds, Clock.systemUTC());
    }

    HmacPaymentCallbackSignatureVerifier(String secret, long maxClockSkewSeconds,
                                         Clock clock) {
        this.secret = secret;
        this.maxClockSkewSeconds = maxClockSkewSeconds;
        this.clock = clock;
    }

    @Override
    public void verify(SignatureInput input) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("payment callback secret is not configured");
        }
        if (input.nonce() == null || input.nonce().isBlank()
                || input.rawBody() == null || input.rawBody().isBlank()
                || input.signature() == null || input.signature().isBlank()) {
            throw new IllegalArgumentException("payment callback signature fields are required");
        }
        if (Math.abs(Instant.now(clock).getEpochSecond() - input.timestamp())
                > maxClockSkewSeconds) {
            throw new IllegalArgumentException("payment callback timestamp expired");
        }
        try {
            String canonical = input.timestamp() + "\n" + input.nonce()
                + "\n" + input.rawBody();
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] expected = mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8));
            byte[] actual = HexFormat.of().parseHex(input.signature());
            if (!MessageDigest.isEqual(expected, actual)) {
                throw new IllegalArgumentException("payment callback signature is invalid");
            }
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("cannot verify payment callback signature", exception);
        }
    }
}
