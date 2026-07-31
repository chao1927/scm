package com.chaobo.scm.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * ScmSecurityProperties。
 *
 * <p>位于公共/base 模块，仅提供稳定的跨模块类型和技术约定，不拥有任何子系统业务状态。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@ConfigurationProperties("scm.security")
public class ScmSecurityProperties {

    /**
     * HMAC-SHA256 生产密钥允许的最小字节长度。
     */
    private static final int MINIMUM_HMAC_SECRET_BYTES = 32;

    /**
     * enabled（类型：{@code boolean}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private boolean enabled = true;

    /**
     * hmacSecret（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String hmacSecret = "";

    private String activeKid = "active";

    private String previousKid = "";

    private String previousHmacSecret = "";

    private long previousValidUntilEpochSecond;

    /**
     * permissionNamespace（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String permissionNamespace = "";

    /**
     * 处理当前类型职责中的操作 {@code isEnabled}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 处理当前类型职责中的操作 {@code setEnabled}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param enabled 业务处理参数或成员，类型为 {@code boolean}
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 处理当前类型职责中的操作 {@code setHmacSecret}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param hmacSecret 业务处理参数或成员，类型为 {@code String}
     */
    public void setHmacSecret(String hmacSecret) {
        this.hmacSecret = hmacSecret == null ? "" : hmacSecret;
    }

    public String getActiveKid() {
        return activeKid;
    }

    public void setActiveKid(String activeKid) {
        this.activeKid = activeKid == null ? "" : activeKid.trim();
    }

    public String getPreviousKid() {
        return previousKid;
    }

    public void setPreviousKid(String previousKid) {
        this.previousKid = previousKid == null ? "" : previousKid.trim();
    }

    public void setPreviousHmacSecret(String previousHmacSecret) {
        this.previousHmacSecret = previousHmacSecret == null ? "" : previousHmacSecret;
    }

    public long getPreviousValidUntilEpochSecond() {
        return previousValidUntilEpochSecond;
    }

    public void setPreviousValidUntilEpochSecond(long previousValidUntilEpochSecond) {
        this.previousValidUntilEpochSecond = previousValidUntilEpochSecond;
    }

    /**
     * 查询并返回 {@code getPermissionNamespace}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code String}
     */
    public String getPermissionNamespace() {
        return permissionNamespace;
    }

    /**
     * 处理当前类型职责中的操作 {@code setPermissionNamespace}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param permissionNamespace 业务处理参数或成员，类型为 {@code String}
     */
    public void setPermissionNamespace(String permissionNamespace) {
        this.permissionNamespace = permissionNamespace == null ? "" : permissionNamespace.trim();
    }

    /**
     * 处理当前类型职责中的操作 {@code secretKey}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SecretKey}
     */
    public SecretKey secretKey() {
        return activeSecretKey();
    }

    public SecretKey activeSecretKey() {
        if (activeKid.isBlank()) {
            throw new IllegalStateException("scm.security.active-kid is required");
        }
        return secretKey(hmacSecret, "scm.security.hmac-secret");
    }

    public Optional<SecretKey> previousSecretKey() {
        if (previousKid.isBlank() && previousHmacSecret.isBlank()) {
            return Optional.empty();
        }
        if (previousKid.isBlank() || previousHmacSecret.isBlank()) {
            throw new IllegalStateException("previous JWT kid and secret must be configured together");
        }
        if (previousKid.equals(activeKid)) {
            throw new IllegalStateException("previous JWT kid must differ from active kid");
        }
        if (previousValidUntilEpochSecond <= 0) {
            throw new IllegalStateException(
                    "scm.security.previous-valid-until-epoch-second must be positive");
        }
        return Optional.of(secretKey(previousHmacSecret, "scm.security.previous-hmac-secret"));
    }

    private static SecretKey secretKey(String secret, String propertyName) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MINIMUM_HMAC_SECRET_BYTES) {
            throw new IllegalStateException(propertyName + " must contain at least 32 bytes");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }
}
