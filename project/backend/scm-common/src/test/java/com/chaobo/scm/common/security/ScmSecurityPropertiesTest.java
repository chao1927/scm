package com.chaobo.scm.common.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScmSecurityPropertiesTest {
    @Test
    void defaultsToEnabledAndRejectsWeakHmacSecret() {
        var properties = new ScmSecurityProperties();

        assertThat(properties.isEnabled()).isTrue();
        assertThatThrownBy(() -> properties.secretKey())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");

        properties.setHmacSecret("01234567890123456789012345678901");
        assertThat(properties.secretKey().getEncoded()).hasSize(32);
    }
}
