package com.chaobo.scm.mdm.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MdmPublicationAggregateTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class MdmPublicationAggregateTest {

    /**
     * 处理当前类型职责中的操作 {@code publicationCanFailRetryAndConfirm}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void publicationCanFailRetryAndConfirm() {
        PublicationAggregate publication = PublicationAggregate.create("PUB400001", "MDV200001V1", "SKU", "SKU-001", "WMS", "mdm.sku.changed");
        publication.fail("目标系统超时");
        publication.retry("人工重试");
        publication.confirm();
        assertThat(publication.status()).isEqualTo(PublicationAggregate.CONFIRMED);
        assertThat(publication.retryCount()).isEqualTo(1);
        assertThat(publication.pullEvents()).extracting(MdmEvent::eventType).containsExactly("MasterDataPublished", "MasterDataRepublished", "MasterDataPublishConfirmed");
    }

    /**
     * 执行命令 {@code retryOnlyWorksForFailedPublication}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void retryOnlyWorksForFailedPublication() {
        PublicationAggregate publication = PublicationAggregate.create("PUB400001", "MDV200001V1", "SKU", "SKU-001", "WMS", "mdm.sku.changed");
        assertThatThrownBy(() -> publication.retry("重试")).isInstanceOf(IllegalStateException.class).hasMessageContaining("not failed");
    }

    /**
     * 处理当前类型职责中的操作 {@code subscriptionDisableUsesOptimisticVersion}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void subscriptionDisableUsesOptimisticVersion() {
        PublicationSubscriptionAggregate subscription = PublicationSubscriptionAggregate.create("SUB300001", "SKU", "WMS", "mdm.sku.changed", null);
        assertThatThrownBy(() -> subscription.disable("停用", 2)).isInstanceOf(IllegalStateException.class).hasMessageContaining("version conflict");
        subscription.disable("停用", 1);
        assertThat(subscription.status()).isEqualTo(PublicationSubscriptionAggregate.DISABLED);
    }
}
