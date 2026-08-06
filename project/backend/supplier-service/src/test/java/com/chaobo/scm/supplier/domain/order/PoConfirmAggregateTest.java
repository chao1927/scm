package com.chaobo.scm.supplier.domain.order;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import static org.assertj.core.api.Assertions.*;

/**
 * PoConfirmAggregateTest。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class PoConfirmAggregateTest {

    /**
     * ids（类型：{@code Ids}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final Ids ids = new Ids();

    /**
     * 处理当前类型职责中的操作 {@code shouldConfirmAllLines}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shouldConfirmAllLines() {
        var a = order();
        var decisions = a.lines().stream().map(v -> new PoConfirmAggregate.LineDecision(v.lineId(), v.orderQty(), LocalDate.now().plusDays(3))).toList();
        a.confirm(decisions, "可按期供货", 1, ids);
        assertThat(a.status()).isEqualTo(PoConfirmStatus.CONFIRMED);
        assertThat(a.lines()).allMatch(v -> v.status() == 2);
    }

    /**
     * 处理当前类型职责中的操作 {@code shouldRejectPartialConfirmation}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shouldRejectPartialConfirmation() {
        var a = order();
        var one = a.lines().get(0);
        assertThatThrownBy(() -> a.confirm(List.of(new PoConfirmAggregate.LineDecision(one.lineId(), one.orderQty(), LocalDate.now().plusDays(3))), null, 1, ids)).isInstanceOf(BusinessException.class);
    }

    /**
     * 处理当前类型职责中的操作 {@code shouldRejectOverConfirmation}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shouldRejectOverConfirmation() {
        var a = order();
        var decisions = a.lines().stream().map(v -> new PoConfirmAggregate.LineDecision(v.lineId(), v.orderQty().add(BigDecimal.ONE), LocalDate.now().plusDays(3))).toList();
        assertThatThrownBy(() -> a.confirm(decisions, null, 1, ids)).isInstanceOf(BusinessException.class);
    }

    /**
     * 验证供应商可以登记行级数量和交期差异，且差异事实保留原因并进入待处理状态。
     */
    @Test
    void shouldReportPurchaseOrderLineDifference() {
        var aggregate = order();
        var line = aggregate.lines().get(0);

        aggregate.feedbackDifference(1, List.of(new PoConfirmAggregate.LineDifference(
                line.lineId(), new BigDecimal("8"), LocalDate.now().plusDays(7), "产能受限")),
                "请采购确认变更", 1, ids);

        assertThat(aggregate.status()).isEqualTo(PoConfirmStatus.DIFFERENCE_PENDING);
        assertThat(aggregate.lines().get(0).confirmedQty()).isEqualByComparingTo("8");
        assertThat(aggregate.pullEvents()).extracting(event -> event.eventType())
                .contains("PurchaseOrderDifferenceReportedBySupplier");
    }

    /**
     * 处理当前类型职责中的操作 {@code order}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PoConfirmAggregate}
     */
    private PoConfirmAggregate order() {
        return PoConfirmAggregate.receive(100, "PO-100", 10, OffsetDateTime.now().plusDays(1), List.of(new PoConfirmAggregate.NewLine("SKU-1", new BigDecimal("10"), LocalDate.now().plusDays(3)), new PoConfirmAggregate.NewLine("SKU-2", new BigDecimal("20"), LocalDate.now().plusDays(5))), 1, ids);
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
         * s（类型：{@code AtomicLong}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final AtomicLong s = new AtomicLong(1);

        /**
         * 处理当前类型职责中的操作 {@code nextId}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
         */
        public long nextId() {
            return s.getAndIncrement();
        }

        /**
         * 处理当前类型职责中的操作 {@code nextBusinessNo}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param p 业务处理参数或成员，类型为 {@code String}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String nextBusinessNo(String p) {
            return p + s.getAndIncrement();
        }
    }
}
