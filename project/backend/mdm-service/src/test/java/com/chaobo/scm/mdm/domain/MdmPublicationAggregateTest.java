package com.chaobo.scm.mdm.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MdmPublicationAggregateTest {
    @Test
    void publicationCanFailRetryAndConfirm() {
        PublicationAggregate publication = PublicationAggregate.create(
                "PUB400001", "MDV200001V1", "SKU", "SKU-001", "WMS", "mdm.sku.changed");

        publication.fail("目标系统超时");
        publication.retry("人工重试");
        publication.confirm();

        assertThat(publication.status()).isEqualTo(PublicationAggregate.CONFIRMED);
        assertThat(publication.retryCount()).isEqualTo(1);
        assertThat(publication.pullEvents()).extracting(MdmEvent::eventType)
                .containsExactly("MasterDataPublished", "MasterDataRepublished", "MasterDataPublishConfirmed");
    }

    @Test
    void retryOnlyWorksForFailedPublication() {
        PublicationAggregate publication = PublicationAggregate.create(
                "PUB400001", "MDV200001V1", "SKU", "SKU-001", "WMS", "mdm.sku.changed");

        assertThatThrownBy(() -> publication.retry("重试"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not failed");
    }

    @Test
    void subscriptionDisableUsesOptimisticVersion() {
        PublicationSubscriptionAggregate subscription = PublicationSubscriptionAggregate.create(
                "SUB300001", "SKU", "WMS", "mdm.sku.changed", null);

        assertThatThrownBy(() -> subscription.disable("停用", 2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("version conflict");

        subscription.disable("停用", 1);
        assertThat(subscription.status()).isEqualTo(PublicationSubscriptionAggregate.DISABLED);
    }
}
