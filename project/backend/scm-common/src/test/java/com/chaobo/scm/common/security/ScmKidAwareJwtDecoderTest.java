package com.chaobo.scm.common.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScmKidAwareJwtDecoderTest {

    @Test
    void verifiesActiveAndPreviousKidButRejectsUnknownKid() throws Exception {
        String activeSecret = "01234567890123456789012345678901";
        String previousSecret = "abcdefghijklmnopqrstuvwxyzABCDEF";
        JwtDecoder active = NimbusJwtDecoder.withSecretKey(
                new SecretKeySpec(activeSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256")).build();
        JwtDecoder previous = NimbusJwtDecoder.withSecretKey(
                new SecretKeySpec(previousSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256")).build();
        ScmKidAwareJwtDecoder decoder = new ScmKidAwareJwtDecoder("active", active,
                Map.of("previous", new ScmKidAwareJwtDecoder.PreviousDecoder(previous,
                        Instant.now().plusSeconds(60).getEpochSecond())));

        assertThat(decoder.decode(token("active", activeSecret)).getSubject()).isEqualTo("1");
        assertThat(decoder.decode(token("previous", previousSecret)).getSubject()).isEqualTo("1");
        assertThatThrownBy(() -> decoder.decode(token("unknown", activeSecret)))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("unknown jwt kid");
    }

    @Test
    void rejectsHeaderWithoutTopLevelKidEvenWhenAnotherValueContainsKidText() throws Exception {
        String activeSecret = "01234567890123456789012345678901";
        JwtDecoder active = NimbusJwtDecoder.withSecretKey(
                new SecretKeySpec(activeSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256")).build();
        ScmKidAwareJwtDecoder decoder = new ScmKidAwareJwtDecoder("active", active, Map.of());

        assertThatThrownBy(() -> decoder.decode(tokenWithHeader(
                "{\"alg\":\"HS256\",\"typ\":\"kid\",\"note\":\"active\"}", activeSecret)))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("jwt kid is required");
    }

    private static String token(String kid, String secret) throws Exception {
        return tokenWithHeader("{\"alg\":\"HS256\",\"typ\":\"JWT\",\"kid\":\"" + kid + "\"}", secret);
    }

    private static String tokenWithHeader(String headerJson, String secret) throws Exception {
        long now = Instant.now().getEpochSecond();
        String header = base64(headerJson);
        String payload = base64("{\"sub\":\"1\",\"iat\":" + now + ",\"exp\":" + (now + 60) + "}");
        String input = header + "." + payload;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return input + "." + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(mac.doFinal(input.getBytes(StandardCharsets.UTF_8)));
    }

    private static String base64(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
