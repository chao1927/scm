package com.chaobo.scm.supplier.domain.quality;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.concurrent.atomic.AtomicLong;
import static org.assertj.core.api.Assertions.*;

/**
 * SupplierQualityIssueAggregateTest。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class SupplierQualityIssueAggregateTest {

    /**
     * ids（类型：{@code Ids}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final Ids ids = new Ids();

    /**
     * 处理当前类型职责中的操作 {@code shouldCompleteRectification}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shouldCompleteRectification() {
        var a = SupplierQualityIssueAggregate.create(1, "WMS", "QC-1", "PERFORMANCE", 3, "性能不合格", 0, ids);
        a.requestRectification(OffsetDateTime.now().plusDays(3), 1, ids);
        a.submitPlan("调整工艺并全检", 2, ids);
        a.verify(true, "复检通过", 3, ids);
        assertThat(a.status()).isEqualTo(QualityIssueStatus.CLOSED);
        assertThat(a.pullEvents()).hasSize(4);
    }

    /**
     * 处理当前类型职责中的操作 {@code shouldRejectBlankPlan}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shouldRejectBlankPlan() {
        var a = SupplierQualityIssueAggregate.create(1, "WMS", "QC-1", "PERFORMANCE", 3, "性能不合格", 0, ids);
        a.requestRectification(OffsetDateTime.now().plusDays(3), 1, ids);
        assertThatThrownBy(() -> a.submitPlan("", 2, ids)).isInstanceOf(BusinessException.class);
    }

    /**
     * 验证整改截止时间已过且仍未提交方案时，质量问题会进入逾期风险状态。
     */
    @Test
    void shouldMarkRectificationOverdue() {
        var aggregate = SupplierQualityIssueAggregate.rehydrate(10, "QI-10", 1, "WMS", "QC-1",
                "PERFORMANCE", 3, "性能不合格", QualityIssueStatus.RECTIFYING.code(),
                OffsetDateTime.now().minusMinutes(1), null, null, 1);

        aggregate.markOverdue(0, ids);

        assertThat(aggregate.status()).isEqualTo(QualityIssueStatus.OVERDUE);
        assertThat(aggregate.pullEvents()).extracting(event -> event.eventType())
                .containsExactly("SupplierRectificationOverdue");
    }

    /**
     * Ids。
     *
     * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static class Ids implements IdentifierGenerator {

        /**
         * n（类型：{@code AtomicLong}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final AtomicLong n = new AtomicLong(1);

        /**
         * 处理当前类型职责中的操作 {@code nextId}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
         */
        public long nextId() {
            return n.getAndIncrement();
        }

        /**
         * 处理当前类型职责中的操作 {@code nextBusinessNo}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param p 业务处理参数或成员，类型为 {@code String}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String nextBusinessNo(String p) {
            return p + n.getAndIncrement();
        }
    }
}
