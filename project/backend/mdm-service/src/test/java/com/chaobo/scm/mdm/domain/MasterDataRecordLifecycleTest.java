package com.chaobo.scm.mdm.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MasterDataRecordLifecycleTest {
    @Test
    void recordReviewApprovalGeneratesAnEnabledVersion() {
        MasterDataRecordAggregate record = MasterDataRecordAggregate.create(
                "MDR200001", "SKU", "SKU-001", "测试商品", "{\"name\":\"测试商品\"}");

        record.submitReview("提交审核", 1);
        record.approve("审核通过", 2);
        MasterDataVersionAggregate version = MasterDataVersionAggregate.generate("MDV200001V1", record, "审核通过");

        assertThat(record.status()).isEqualTo(MasterDataRecordAggregate.ENABLED);
        assertThat(record.currentVersionNo()).isEqualTo(1);
        assertThat(version.versionNumber()).isEqualTo(1);
        assertThat(record.pullEvents()).extracting(MdmEvent::eventType)
                .containsExactly("MasterDataDraftCreated", "MasterDataSubmitted", "MasterDataEnabled");
        assertThat(version.pullEvents()).extracting(MdmEvent::eventType)
                .containsExactly("MasterDataVersionGenerated");
    }

    @Test
    void onlyDraftOrRejectedRecordsCanBeChanged() {
        MasterDataRecordAggregate record = MasterDataRecordAggregate.create(
                "MDR200001", "SKU", "SKU-001", "测试商品", "{\"name\":\"测试商品\"}");
        record.submitReview("提交审核", 1);

        assertThatThrownBy(() -> record.change("改名", "{\"name\":\"改名\"}", "补充资料", 2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not editable");
    }

    @Test
    void rejectFreezeAndDisableNeedValidStateAndReason() {
        MasterDataRecordAggregate record = MasterDataRecordAggregate.create(
                "MDR200001", "SKU", "SKU-001", "测试商品", "{\"name\":\"测试商品\"}");

        assertThatThrownBy(() -> record.freeze("冻结", 1)).isInstanceOf(IllegalStateException.class);

        record.submitReview("提交审核", 1);
        assertThatThrownBy(() -> record.reject("", 2)).isInstanceOf(IllegalArgumentException.class);
        record.approve("通过", 2);
        record.freeze("质量冻结", 3);
        record.disable("停用", 4);

        assertThat(record.status()).isEqualTo(MasterDataRecordAggregate.DISABLED);
    }
}
