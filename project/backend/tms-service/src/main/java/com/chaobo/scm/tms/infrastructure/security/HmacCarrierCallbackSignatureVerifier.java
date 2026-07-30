package com.chaobo.scm.tms.infrastructure.security;

import com.chaobo.scm.tms.application.CarrierCallbackSignatureVerifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;

/**
 * 基于 HMAC-SHA256 的承运商回调验签器。
 *
 * <p>密钥从 {@code scm.tms.carrier-callback.secrets.<carrierCode>} 读取，可由 Nacos 覆盖。
 * 未配置密钥、时间戳超出窗口或签名不匹配均失败关闭。
 *
 * @author SCM Team
 */
@Component
public class HmacCarrierCallbackSignatureVerifier implements CarrierCallbackSignatureVerifier {

    private static final String HMAC_SHA_256 = "HmacSHA256";
    private final Environment environment;
    private final Clock clock;

    public HmacCarrierCallbackSignatureVerifier(Environment environment) {
        this(environment, Clock.systemUTC());
    }

    HmacCarrierCallbackSignatureVerifier(Environment environment, Clock clock) {
        this.environment = environment;
        this.clock = clock;
    }

    @Override
    public void verify(SignatureInput input) {
        requireText(input.carrierCode(), "carrier code");
        requireText(input.nonce(), "callback nonce");
        requireText(input.rawBody(), "callback body");
        requireText(input.signature(), "callback signature");
        long maxSkew = environment.getProperty(
            "scm.tms.carrier-callback.max-clock-skew-seconds", Long.class, 300L);
        long now = Instant.now(clock).getEpochSecond();
        if (Math.abs(now - input.timestamp()) > maxSkew) {
            throw new IllegalArgumentException("carrier callback timestamp expired");
        }
        String key = "scm.tms.carrier-callback.secrets." + input.carrierCode();
        String secret = environment.getProperty(key);
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("carrier callback secret is not configured");
        }
        String canonical = input.carrierCode() + "\n" + input.timestamp() + "\n"
            + input.nonce() + "\n" + input.rawBody();
        byte[] expected = hmac(secret, canonical);
        byte[] actual;
        try {
            actual = HexFormat.of().parseHex(input.signature().trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("carrier callback signature is invalid", exception);
        }
        if (!MessageDigest.isEqual(expected, actual)) {
            throw new IllegalArgumentException("carrier callback signature is invalid");
        }
    }

    private static byte[] hmac(String secret, String canonical) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA_256);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA_256));
            return mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("cannot calculate carrier callback signature", exception);
        }
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
    }
}
