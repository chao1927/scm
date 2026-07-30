package com.chaobo.scm.supplier.domain.quote;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import org.junit.jupiter.api.Test;
import java.math.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import static org.assertj.core.api.Assertions.*;

/**
 * SupplierQuoteAggregateTest。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class SupplierQuoteAggregateTest {

    /**
     * ids（类型：{@code Ids}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final Ids ids = new Ids();

    /**
     * 处理当前类型职责中的操作 {@code quote}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierQuoteAggregate}
     */
    private SupplierQuoteAggregate quote() {
        return SupplierQuoteAggregate.create(1, 10L, "RFQ-1", "CNY", LocalDate.now(), LocalDate.now().plusDays(10), List.of(new QuoteLine(2, "SKU-1", BigDecimal.TEN, new BigDecimal("12.30"), new BigDecimal("13"), 3, BigDecimal.ONE)), 1, ids);
    }

    /**
     * 处理当前类型职责中的操作 {@code shouldSubmitConfirmAndAdopt}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shouldSubmitConfirmAndAdopt() {
        var q = quote();
        q.submit(1, ids);
        q.confirm(2, ids);
        q.adopt("PA-1", 2, ids);
        assertThat(q.status()).isEqualTo(QuoteStatus.ADOPTED);
        assertThat(q.pullEvents()).hasSize(4);
    }

    /**
     * 处理当前类型职责中的操作 {@code shouldModifyOnlyDraftAndRaiseEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shouldModifyOnlyDraftAndRaiseEvent() {
        var q = quote();
        q.pullEvents();
        q.modifyDraft(LocalDate.now().plusDays(1), LocalDate.now().plusDays(20), List.of(new QuoteLine(2, "SKU-1", new BigDecimal("20"), new BigDecimal("11.50"), new BigDecimal("13"), 2, BigDecimal.ONE)), 1, ids);
        assertThat(q.validTo()).isEqualTo(LocalDate.now().plusDays(20));
        assertThat(q.lines().get(0).unitPrice()).isEqualByComparingTo("11.50");
        assertThat(q.pullEvents()).extracting(e -> e.eventType()).containsExactly("SupplierQuoteDraftModified");
        q.submit(1, ids);
        assertThatThrownBy(() -> q.modifyDraft(LocalDate.now(), LocalDate.now().plusDays(2), q.lines(), 1, ids)).isInstanceOf(BusinessException.class);
    }

    /**
     * 处理当前类型职责中的操作 {@code shouldNotAdoptBeforeConfirm}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shouldNotAdoptBeforeConfirm() {
        var q = quote();
        assertThatThrownBy(() -> q.adopt("PA-1", 1, ids)).isInstanceOf(BusinessException.class);
    }

    /**
     * 处理当前类型职责中的操作 {@code shouldRejectSubmittedQuote}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shouldRejectSubmittedQuote() {
        var q = quote();
        q.submit(1, ids);
        q.reject("价格偏高", 2, ids);
        assertThat(q.status()).isEqualTo(QuoteStatus.REJECTED);
    }

    /**
     * Ids。
     *
     * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static final class Ids implements IdentifierGenerator {

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
