package com.chaobo.scm.mdm.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MdmImportQualityAggregateTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class MdmImportQualityAggregateTest {

    /**
     * 处理当前类型职责中的操作 {@code importTaskCanValidateExecuteAndFinishAsPartialFailed}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void importTaskCanValidateExecuteAndFinishAsPartialFailed() {
        ImportTaskAggregate task = ImportTaskAggregate.create("IMP500001", "SKU", "sku.csv", "oss://sku.csv", "hash-1", "CREATE", false, "REJECT");
        task.validateFile(3, 1, "oss://sku-error.csv", 1);
        task.execute(2);
        task.complete(3);
        assertThat(task.status()).isEqualTo(ImportTaskAggregate.PARTIAL_FAILED);
        assertThat(task.successCount()).isEqualTo(2);
        assertThat(task.pullEvents()).extracting(MdmEvent::eventType).containsExactly("ImportTaskCreated", "ImportFileValidated", "ImportTaskExecuted", "ImportTaskCompleted");
    }

    /**
     * 处理当前类型职责中的操作 {@code importTaskCanOnlyCancelBeforeExecution}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void importTaskCanOnlyCancelBeforeExecution() {
        ImportTaskAggregate task = ImportTaskAggregate.create("IMP500001", "SKU", "sku.csv", "oss://sku.csv", "hash-1", "CREATE", false, "REJECT");
        task.validateFile(1, 0, null, 1);
        task.execute(2);
        assertThatThrownBy(() -> task.cancel("取消", 3)).isInstanceOf(IllegalStateException.class).hasMessageContaining("cannot be cancelled");
    }

    /**
     * 处理当前类型职责中的操作 {@code qualityIssueRequiresAssignFixVerifyBeforeClose}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void qualityIssueRequiresAssignFixVerifyBeforeClose() {
        DataQualityIssueAggregate issue = DataQualityIssueAggregate.raise("DQI700001", "SKU", "SKU-001", "MISSING_FIELD", "缺少税率");
        assertThatThrownBy(() -> issue.close(1)).isInstanceOf(IllegalStateException.class);
        issue.assign(1001L, 1);
        issue.markFixed("已补充税率", 2);
        issue.verify(3);
        issue.close(4);
        assertThat(issue.status()).isEqualTo(DataQualityIssueAggregate.CLOSED);
        assertThat(issue.pullEvents()).extracting(MdmEvent::eventType).contains("DataQualityIssueRaised", "DataQualityIssueClosed");
    }
}
