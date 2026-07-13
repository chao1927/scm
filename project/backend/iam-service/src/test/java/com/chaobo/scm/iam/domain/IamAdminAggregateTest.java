package com.chaobo.scm.iam.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IamAdminAggregateTest {
    @Test
    void applicationCanChangeAndDisableWithVersion() {
        IamApplicationAggregate app = IamApplicationAggregate.create("OMS", "订单系统", "/oms");

        app.change("订单履约系统", "/oms/home", 1);
        app.disable(2);

        assertThat(app.status()).isEqualTo(IamApplicationAggregate.DISABLED);
        assertThat(app.version()).isEqualTo(3);
    }

    @Test
    void applicationRejectsVersionConflict() {
        IamApplicationAggregate app = IamApplicationAggregate.create("OMS", "订单系统", "/oms");

        assertThatThrownBy(() -> app.disable(2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("version conflict");
    }

    @Test
    void ssoClientResetsSecretWithVersion() {
        IamSsoClientAggregate client = IamSsoClientAggregate.configure(
                "OMS-WEB", "OMS", "https://oms.example/callback", "HASH:old");

        client.resetSecret("HASH:new", 1);

        assertThat(client.secretHash()).isEqualTo("HASH:new");
        assertThat(client.version()).isEqualTo(2);
    }
}
