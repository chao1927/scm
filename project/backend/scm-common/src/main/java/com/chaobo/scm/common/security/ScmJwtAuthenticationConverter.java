package com.chaobo.scm.common.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ScmJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Set<String> permissions = stringSet(jwt.getClaim("permissions"));
        var authorities = permissions.stream().map(SimpleGrantedAuthority::new).toList();
        var authentication = new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
        authentication.setDetails(new ScmAccessContext(operatorId(jwt), jwt.getClaimAsString("username"),
                jwt.getClaimAsString("app"), permissions, dataScopes(jwt.getClaim("data_scopes"))));
        return authentication;
    }

    private static long operatorId(Jwt jwt) {
        try {
            return Long.parseLong(jwt.getSubject());
        } catch (RuntimeException exception) {
            return 0L;
        }
    }

    private static Map<String, Set<String>> dataScopes(Object claim) {
        if (!(claim instanceof Map<?, ?> rawScopes)) {
            return Map.of();
        }
        Map<String, Set<String>> scopes = new LinkedHashMap<>();
        rawScopes.forEach((key, value) -> scopes.put(String.valueOf(key), stringSet(value)));
        return Map.copyOf(scopes);
    }

    private static Set<String> stringSet(Object value) {
        if (value instanceof Collection<?> collection) {
            Set<String> values = new LinkedHashSet<>();
            collection.stream().map(String::valueOf).filter(item -> !item.isBlank()).forEach(values::add);
            return Set.copyOf(values);
        }
        if (value instanceof String text && !text.isBlank()) {
            Set<String> values = new LinkedHashSet<>();
            for (String item : text.split("[,\\s]+")) {
                if (!item.isBlank()) {
                    values.add(item);
                }
            }
            return Set.copyOf(values);
        }
        return Set.of();
    }
}
