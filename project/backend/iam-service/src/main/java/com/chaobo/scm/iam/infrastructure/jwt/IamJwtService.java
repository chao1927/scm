package com.chaobo.scm.iam.infrastructure.jwt;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * IamJwtService。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class IamJwtService {

    /**
     * MINIMUM_SECRET_BYTES（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    private static final int MINIMUM_SECRET_BYTES = 32;

    /**
     * secret（类型：{@code byte[]}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final byte[] secret;

    /**
     * json（类型：{@code ObjectMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final ObjectMapper json = new ObjectMapper();

    /**
     * 创建 IamJwtService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param secret 业务处理参数或成员，类型为 {@code String}
     */
    public IamJwtService(String secret) {
        byte[] candidate = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
        if (candidate.length < MINIMUM_SECRET_BYTES) {
            throw new IllegalArgumentException("IAM JWT signing secret must contain at least 32 bytes");
        }
        this.secret = candidate.clone();
    }

    /**
     * 处理当前类型职责中的操作 {@code issue}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param claims 业务处理参数或成员，类型为 {@code TokenClaims}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String issue(TokenClaims claims) {
        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", claims.subject());
        payload.put("username", claims.username());
        payload.put("app", claims.appCode());
        payload.put("jti", claims.jti());
        payload.put("type", claims.tokenType());
        payload.put("iat", claims.issuedAtEpochSecond());
        payload.put("exp", claims.expiresAtEpochSecond());
        payload.put("permissions", claims.permissions());
        payload.put("data_scopes", claims.dataScopes());
        try {
            String signingInput = base64Url(json.writeValueAsBytes(header)) + "." + base64Url(json.writeValueAsBytes(payload));
            return signingInput + "." + sign(signingInput);
        } catch (JacksonException exception) {
            throw new IllegalStateException("jwt serialization failed", exception);
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code verify}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param token 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code TokenClaims}
     */
    @SuppressWarnings("unchecked")
    public TokenClaims verify(String token) {
        String[] parts = token == null ? new String[0] : token.split("\\.");
        if (parts.length != VERIFY_VALUE_3) {
            throw new IllegalArgumentException("invalid jwt format");
        }
        String signingInput = parts[0] + "." + parts[1];
        if (!constantTimeEquals(sign(signingInput), parts[VERIFY_VALUE_2])) {
            throw new IllegalArgumentException("invalid jwt signature");
        }
        try {
            Map<String, Object> values = json.readValue(Base64.getUrlDecoder().decode(parts[1]), Map.class);
            long exp = number(values.get("exp"));
            if (Instant.now().getEpochSecond() >= exp) {
                throw new IllegalArgumentException("jwt expired");
            }
            Set<String> permissions = stringSet(values.get("permissions"));
            Map<String, Set<String>> dataScopes = new LinkedHashMap<>();
            if (values.get(DATA_SCOPES) instanceof Map<?, ?> rawScopes) {
                rawScopes.forEach((key, value) -> dataScopes.put(String.valueOf(key), stringSet(value)));
            }
            return new TokenClaims(text(values, "sub"), text(values, "username"), text(values, "app"), text(values, "jti"), text(values, "type"), number(values.get("iat")), exp, permissions, dataScopes);
        } catch (RuntimeException exception) {
            if (exception instanceof IllegalArgumentException invalid) {
                throw invalid;
            }
            throw new IllegalArgumentException("invalid jwt payload", exception);
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code sign}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param signingInput 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String sign(String signingInput) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return base64Url(mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("jwt signing failed", exception);
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code base64Url}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param bytes 业务处理参数或成员，类型为 {@code byte[]}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * 处理当前类型职责中的操作 {@code constantTimeEquals}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param left 业务处理参数或成员，类型为 {@code String}
     * @param right 业务处理参数或成员，类型为 {@code String}
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    private static boolean constantTimeEquals(String left, String right) {
        if (left == null || right == null || left.length() != right.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < left.length(); i++) {
            result |= left.charAt(i) ^ right.charAt(i);
        }
        return result == 0;
    }

    /**
     * 处理当前类型职责中的操作 {@code text}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param values 业务处理参数或成员，类型为 {@code Map<String,Object>}
     * @param key 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String text(Map<String, Object> values, String key) {
        Object value = values.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    /**
     * 处理当前类型职责中的操作 {@code number}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code Object}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    private static long number(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    /**
     * 处理当前类型职责中的操作 {@code stringSet}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code Object}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Set<String>}
     */
    private static Set<String> stringSet(Object value) {
        if (!(value instanceof Iterable<?> values)) {
            return Set.of();
        }
        Set<String> result = new LinkedHashSet<>();
        values.forEach(item -> result.add(String.valueOf(item)));
        return Set.copyOf(result);
    }

    /**
     * TokenClaims。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record TokenClaims(String subject, String username, String appCode, String jti, String tokenType, long issuedAtEpochSecond, long expiresAtEpochSecond, Set<String> permissions, Map<String, Set<String>> dataScopes) {

        public TokenClaims {
            permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
            if (dataScopes == null || dataScopes.isEmpty()) {
                dataScopes = Map.of();
            } else {
                Map<String, Set<String>> copy = new LinkedHashMap<>();
                dataScopes.forEach((key, values) -> copy.put(key, values == null ? Set.of() : Set.copyOf(values)));
                dataScopes = Map.copyOf(copy);
            }
        }

        /**
         * 创建 TokenClaims。
         *
         * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
         * @param subject 业务处理参数或成员，类型为 {@code String}
         * @param username 业务处理参数或成员，类型为 {@code String}
         * @param appCode 可追踪业务编码，类型为 {@code String}
         * @param jti 业务处理参数或成员，类型为 {@code String}
         * @param tokenType 业务处理参数或成员，类型为 {@code String}
         * @param issuedAtEpochSecond 业务时间，类型为 {@code long}
         * @param expiresAtEpochSecond 业务处理参数或成员，类型为 {@code long}
         */
        public TokenClaims(String subject, String username, String appCode, String jti, String tokenType, long issuedAtEpochSecond, long expiresAtEpochSecond) {
            this(subject, username, appCode, jti, tokenType, issuedAtEpochSecond, expiresAtEpochSecond, Set.of(), Map.of());
        }
    }

    /**
     * 业务常量 {@code DATA_SCOPES}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String DATA_SCOPES = "data_scopes";

    /**
     * 业务常量 {@code VERIFY_VALUE_2}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int VERIFY_VALUE_2 = 2;

    /**
     * 业务常量 {@code VERIFY_VALUE_3}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int VERIFY_VALUE_3 = 3;
}
