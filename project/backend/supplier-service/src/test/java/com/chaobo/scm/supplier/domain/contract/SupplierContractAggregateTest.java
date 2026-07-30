package com.chaobo.scm.supplier.domain.contract;

import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.concurrent.atomic.*;
import static org.assertj.core.api.Assertions.*;

/**
 * SupplierContractAggregateTest。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class SupplierContractAggregateTest {

    /**
     * ids（类型：{@code TestIdentifierGenerator}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final TestIdentifierGenerator ids = new TestIdentifierGenerator();

    /**
     * 处理当前类型职责中的操作 {@code shouldApproveRenewAndTerminate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shouldApproveRenewAndTerminate() {
        var c = SupplierContractAggregate.create(1, 2L, "PA-1", "FRAMEWORK", LocalDate.now(), LocalDate.now().plusDays(30), "付款及质量条款", "https://a", 1, ids);
        c.submit(1, ids);
        c.approve(2, ids);
        c.renew(LocalDate.now().plusDays(60), 2, ids);
        c.terminate("合作结束", 2, ids);
        assertThat(c.status()).isEqualTo(ContractStatus.TERMINATED);
        assertThat(c.pullEvents()).hasSize(5);
    }

    /**
     * 处理当前类型职责中的操作 {@code shouldModifyDraftAndRejectModificationAfterSubmit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shouldModifyDraftAndRejectModificationAfterSubmit() {
        var c = SupplierContractAggregate.create(1, null, null, "FRAMEWORK", LocalDate.now(), LocalDate.now().plusDays(30), "原条款", "https://old", 1, ids);
        c.pullEvents();
        c.modifyDraft(LocalDate.now().plusDays(45), "新条款", "https://new", 2, ids);
        assertThat(c.to()).isEqualTo(LocalDate.now().plusDays(45));
        assertThat(c.terms()).isEqualTo("新条款");
        assertThat(c.attachment()).isEqualTo("https://new");
        assertThat(c.pullEvents()).extracting(e -> e.eventType()).containsExactly("SupplierContractDraftModified");
        c.submit(2, ids);
        assertThatThrownBy(() -> c.modifyDraft(LocalDate.now().plusDays(50), "再次修改", "https://new", 2, ids)).isInstanceOf(com.chaobo.scm.common.error.BusinessException.class);
    }

    /**
     * 处理当前类型职责中的操作 {@code shouldExpireActivePastContract}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shouldExpireActivePastContract() {
        var c = SupplierContractAggregate.create(1, null, null, "FRAMEWORK", LocalDate.now().minusDays(10), LocalDate.now().minusDays(1), "付款条款", "https://a", 1, ids);
        c.submit(1, ids);
        c.approve(1, ids);
        c.expire(0, ids);
        assertThat(c.status()).isEqualTo(ContractStatus.EXPIRED);
        assertThat(c.pullEvents()).extracting(e -> e.eventType()).contains("SupplierContractExpired");
    }

    /**
     * 处理当前类型职责中的操作 {@code shouldReturnToDraftOnApprovalRejection}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shouldReturnToDraftOnApprovalRejection() {
        var c = SupplierContractAggregate.create(1, null, null, "FRAMEWORK", LocalDate.now(), LocalDate.now().plusDays(30), "付款条款", "https://a", 1, ids);
        c.submit(1, ids);
        c.rejectApproval("附件签章不完整", 2, ids);
        assertThat(c.status()).isEqualTo(ContractStatus.DRAFT);
    }

    /**
     * 测试专用标识生成器。
     *
     * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    static class TestIdentifierGenerator implements IdentifierGenerator {

        /**
         * n（类型：{@code AtomicLong}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        AtomicLong n = new AtomicLong();

        /**
         * 处理当前类型职责中的操作 {@code nextId}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
         */
        public long nextId() {
            return n.incrementAndGet();
        }

        /**
         * 处理当前类型职责中的操作 {@code nextBusinessNo}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param p 业务处理参数或成员，类型为 {@code String}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String nextBusinessNo(String p) {
            return p + nextId();
        }
    }
}
