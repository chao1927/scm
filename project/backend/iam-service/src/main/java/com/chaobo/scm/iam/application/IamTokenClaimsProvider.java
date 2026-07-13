package com.chaobo.scm.iam.application;

import com.chaobo.scm.iam.infrastructure.persistence.IamPermissionOpenApiMapper;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@FunctionalInterface
public interface IamTokenClaimsProvider {
    PermissionClaims claimsFor(long userId);

    record PermissionClaims(Set<String> permissions, Map<String, Set<String>> dataScopes) {
        public PermissionClaims {
            permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
            dataScopes = dataScopes == null ? Map.of() : Map.copyOf(dataScopes);
        }
    }

    @Component
    final class DatabaseIamTokenClaimsProvider implements IamTokenClaimsProvider {
        private final IamPermissionOpenApiMapper mapper;

        public DatabaseIamTokenClaimsProvider(IamPermissionOpenApiMapper mapper) {
            this.mapper = mapper;
        }

        @Override
        public PermissionClaims claimsFor(long userId) {
            Set<String> permissions = new LinkedHashSet<>();
            mapper.permissionGrants(userId).forEach(grant -> permissions.add(grant.permissionCode()));
            Map<String, Set<String>> dataScopes = new LinkedHashMap<>();
            mapper.dataScopeGrants(userId).forEach(grant -> dataScopes
                    .computeIfAbsent(grant.scopeType(), ignored -> new LinkedHashSet<>())
                    .add(grant.scopeValue()));
            return new PermissionClaims(permissions, dataScopes);
        }
    }
}
