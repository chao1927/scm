package com.chaobo.scm.mdm.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MdmImportQualityAggregateTest {
    @Test
    void importTaskCanValidateExecuteAndFinishAsPartialFailed() {
        ImportTaskAggregate task = ImportTaskAggregate.create(
                "IMP500001", "SKU", "sku.csv", "oss://sku.csv", "hash-1", "CREATE", false, "REJECT");

        task.validateFile(3, 1, "oss://sku-error.csv", 1);
        task.execute(2);
        task.complete(3);

        assertThat(task.status()).isEqualTo(ImportTaskAggregate.PARTIAL_FAILED);
        assertThat(task.successCount()).isEqualTo(2);
        assertThat(task.pullEvents()).extracting(MdmEvent::eventType)
                .containsExactly("ImportTaskCreated", "ImportFileValidated", "ImportTaskExecuted", "ImportTaskCompleted");
    }

    @Test
    void importTaskCanOnlyCancelBeforeExecution() {
        ImportTaskAggregate task = ImportTaskAggregate.create(
                "IMP500001", "SKU", "sku.csv", "oss://sku.csv", "hash-1", "CREATE", false, "REJECT");
        task.validateFile(1, 0, null, 1);
        task.execute(2);

        assertThatThrownBy(() -> task.cancel("取消", 3))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be cancelled");
    }

    @Test
    void qualityIssueRequiresAssignFixVerifyBeforeClose() {
        DataQualityIssueAggregate issue = DataQualityIssueAggregate.raise(
                "DQI700001", "SKU", "SKU-001", "MISSING_FIELD", "缺少税率");

        assertThatThrownBy(() -> issue.close(1)).isInstanceOf(IllegalStateException.class);

        issue.assign(1001L, 1);
        issue.markFixed("已补充税率", 2);
        issue.verify(3);
        issue.close(4);

        assertThat(issue.status()).isEqualTo(DataQualityIssueAggregate.CLOSED);
        assertThat(issue.pullEvents()).extracting(MdmEvent::eventType)
                .contains("DataQualityIssueRaised", "DataQualityIssueClosed");
    }
}
