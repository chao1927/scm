package com.chaobo.scm.common.security;

import com.nimbusds.jwt.SignedJWT;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.text.ParseException;
import java.time.Instant;
import java.util.Map;

/** Selects the verification key strictly by JWT kid. */
final class ScmKidAwareJwtDecoder implements JwtDecoder {

    private final String activeKid;
    private final JwtDecoder activeDecoder;
    private final Map<String, PreviousDecoder> previousDecoders;

    ScmKidAwareJwtDecoder(String activeKid, JwtDecoder activeDecoder,
                          Map<String, PreviousDecoder> previousDecoders) {
        if (activeKid == null || activeKid.isBlank()) {
            throw new IllegalStateException("scm.security.active-kid is required");
        }
        this.activeKid = activeKid;
        this.activeDecoder = activeDecoder;
        this.previousDecoders = Map.copyOf(previousDecoders);
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        String kid = keyId(token);
        if (activeKid.equals(kid)) {
            return activeDecoder.decode(token);
        }
        PreviousDecoder previous = previousDecoders.get(kid);
        if (previous == null) {
            throw new JwtException("unknown jwt kid");
        }
        if (Instant.now().getEpochSecond() > previous.validUntilEpochSecond()) {
            throw new JwtException("previous jwt verification window expired");
        }
        return previous.decoder().decode(token);
    }

    private static String keyId(String token) {
        try {
            String kid = SignedJWT.parse(token).getHeader().getKeyID();
            if (kid == null || kid.isBlank()) {
                throw new JwtException("jwt kid is required");
            }
            return kid;
        } catch (ParseException exception) {
            throw new JwtException("invalid jwt header", exception);
        }
    }

    record PreviousDecoder(JwtDecoder decoder, long validUntilEpochSecond) {
    }
}
