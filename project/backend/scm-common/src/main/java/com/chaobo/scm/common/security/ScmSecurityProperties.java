package com.chaobo.scm.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@ConfigurationProperties("scm.security")
public class ScmSecurityProperties {
    private boolean enabled = true;
    private String hmacSecret = "";
    private String permissionNamespace = "";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setHmacSecret(String hmacSecret) {
        this.hmacSecret = hmacSecret == null ? "" : hmacSecret;
    }

    public String getPermissionNamespace() {
        return permissionNamespace;
    }

    public void setPermissionNamespace(String permissionNamespace) {
        this.permissionNamespace = permissionNamespace == null ? "" : permissionNamespace.trim();
    }

    public SecretKey secretKey() {
        byte[] bytes = hmacSecret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("scm.security.hmac-secret must contain at least 32 bytes");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }
}
