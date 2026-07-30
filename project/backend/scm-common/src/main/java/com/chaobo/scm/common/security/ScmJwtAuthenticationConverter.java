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

/**
 * ScmJwtAuthenticationConverter。
 *
 * <p>位于公共/base 模块，仅提供稳定的跨模块类型和技术约定，不拥有任何子系统业务状态。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class ScmJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    /**
     * JWT 中逗号或空白分隔权限文本的切分表达式。
     */
    private static final String CLAIM_VALUE_SEPARATOR = "[,\\s]+";

    /**
     * 处理当前类型职责中的操作 {@code convert}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param jwt 业务处理参数或成员，类型为 {@code Jwt}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code AbstractAuthenticationToken}
     */
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Set<String> permissions = stringSet(jwt.getClaim("permissions"));
        var authorities = permissions.stream().map(SimpleGrantedAuthority::new).toList();
        var authentication = new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
        authentication.setDetails(new ScmAccessContext(operatorId(jwt), jwt.getClaimAsString("username"), jwt.getClaimAsString("app"), permissions, dataScopes(jwt.getClaim("data_scopes"))));
        return authentication;
    }

    /**
     * 处理当前类型职责中的操作 {@code operatorId}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param jwt 业务处理参数或成员，类型为 {@code Jwt}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    private static long operatorId(Jwt jwt) {
        try {
            return Long.parseLong(jwt.getSubject());
        } catch (RuntimeException exception) {
            return 0L;
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code dataScopes}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param claim 业务处理参数或成员，类型为 {@code Object}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Map<String,Set<String>>}
     */
    private static Map<String, Set<String>> dataScopes(Object claim) {
        if (!(claim instanceof Map<?, ?> rawScopes)) {
            return Map.of();
        }
        Map<String, Set<String>> scopes = new LinkedHashMap<>();
        rawScopes.forEach((key, value) -> scopes.put(String.valueOf(key), stringSet(value)));
        return Map.copyOf(scopes);
    }

    /**
     * 处理当前类型职责中的操作 {@code stringSet}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code Object}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Set<String>}
     */
    private static Set<String> stringSet(Object value) {
        if (value instanceof Collection<?> collection) {
            Set<String> values = new LinkedHashSet<>();
            collection.stream().map(String::valueOf).filter(item -> !item.isBlank()).forEach(values::add);
            return Set.copyOf(values);
        }
        if (value instanceof String text && !text.isBlank()) {
            Set<String> values = new LinkedHashSet<>();
            for (String item : text.split(CLAIM_VALUE_SEPARATOR)) {
                if (!item.isBlank()) {
                    values.add(item);
                }
            }
            return Set.copyOf(values);
        }
        return Set.of();
    }
}
