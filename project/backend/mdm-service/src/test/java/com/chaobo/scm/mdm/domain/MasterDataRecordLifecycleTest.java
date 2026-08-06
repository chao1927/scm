package com.chaobo.scm.mdm.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MasterDataRecordLifecycleTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class MasterDataRecordLifecycleTest {

    /**
     * 处理当前类型职责中的操作 {@code recordReviewApprovalGeneratesAnEnabledVersion}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void recordReviewApprovalGeneratesAnEnabledVersion() {
        MasterDataRecordAggregate record = MasterDataRecordAggregate.create("MDR200001", "SKU", "SKU-001", "测试商品", "{\"name\":\"测试商品\"}");
        record.submitReview("提交审核", 1);
        record.approve("审核通过", 2);
        MasterDataVersionAggregate version = MasterDataVersionAggregate.generate("MDV200001V1", record, "审核通过");
        assertThat(record.status()).isEqualTo(MasterDataRecordAggregate.ENABLED);
        assertThat(record.currentVersionNo()).isEqualTo(1);
        assertThat(version.versionNumber()).isEqualTo(1);
        assertThat(record.pullEvents()).extracting(MdmEvent::eventType).containsExactly("MasterDataDraftCreated", "MasterDataSubmitted", "MasterDataEnabled");
        assertThat(version.pullEvents()).extracting(MdmEvent::eventType).containsExactly("MasterDataVersionGenerated");
    }

    /**
     * 处理当前类型职责中的操作 {@code onlyDraftOrRejectedRecordsCanBeChanged}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void onlyDraftOrRejectedRecordsCanBeChanged() {
        MasterDataRecordAggregate record = MasterDataRecordAggregate.create("MDR200001", "SKU", "SKU-001", "测试商品", "{\"name\":\"测试商品\"}");
        record.submitReview("提交审核", 1);
        assertThatThrownBy(() -> record.change("改名", "{\"name\":\"改名\"}", "补充资料", 2)).isInstanceOf(IllegalStateException.class).hasMessageContaining("not editable");
    }

    /**
     * 执行命令 {@code rejectFreezeAndDisableNeedValidStateAndReason}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void rejectFreezeAndDisableNeedValidStateAndReason() {
        MasterDataRecordAggregate record = MasterDataRecordAggregate.create("MDR200001", "SKU", "SKU-001", "测试商品", "{\"name\":\"测试商品\"}");
        assertThatThrownBy(() -> record.freeze("冻结", 1)).isInstanceOf(IllegalStateException.class);
        record.submitReview("提交审核", 1);
        assertThatThrownBy(() -> record.reject("", 2)).isInstanceOf(IllegalArgumentException.class);
        record.approve("通过", 2);
        record.freeze("质量冻结", 3);
        record.disable("停用", 4);
        assertThat(record.status()).isEqualTo(MasterDataRecordAggregate.DISABLED);
    }

    /** 冻结中的主数据允许被明确启用，并生成可审计领域事件。 */
    @Test
    void frozenRecordCanBeEnabledAgain() {
        MasterDataRecordAggregate record = MasterDataRecordAggregate.create("MDR200001", "SKU",
                "SKU-001", "测试商品", "{\"name\":\"测试商品\"}");
        record.submitReview("提交审核", 1);
        record.approve("审核通过", 2);
        record.freeze("质量冻结", 3);

        record.enable("复核通过", 4);

        assertThat(record.status()).isEqualTo(MasterDataRecordAggregate.ENABLED);
        assertThat(record.version()).isEqualTo(5);
        assertThat(record.pullEvents()).extracting(MdmEvent::eventType)
                .contains("MasterDataEnabled");
    }
}
