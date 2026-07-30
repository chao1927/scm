package com.chaobo.scm.supplier.domain.qualification;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.concurrent.atomic.*;
import static org.assertj.core.api.Assertions.*;

/**
 * SupplierQualificationAggregateTest。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class SupplierQualificationAggregateTest {

    /**
     * ids（类型：{@code Ids}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final Ids ids = new Ids();

    /**
     * 处理当前类型职责中的操作 {@code qualification}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierQualificationAggregate}
     */
    private SupplierQualificationAggregate qualification() {
        return SupplierQualificationAggregate.submit(1, "BUSINESS_LICENSE", "91310000", LocalDate.now().minusDays(1), LocalDate.now().plusDays(30), "https://example/license.pdf", 1, ids);
    }

    /**
     * 处理当前类型职责中的操作 {@code shouldApproveValidQualification}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shouldApproveValidQualification() {
        var a = qualification();
        a.approve("资料齐全", 2, ids);
        assertThat(a.status()).isEqualTo(QualificationStatus.VALID);
        assertThat(a.pullEvents()).hasSize(2);
    }

    /**
     * 处理当前类型职责中的操作 {@code shouldRejectWithoutReason}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shouldRejectWithoutReason() {
        var a = qualification();
        assertThatThrownBy(() -> a.reject("", 2, ids)).isInstanceOf(BusinessException.class);
    }

    /**
     * 处理当前类型职责中的操作 {@code shouldExpireOnlyAfterValidTo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shouldExpireOnlyAfterValidTo() {
        var a = qualification();
        a.approve("", 2, ids);
        a.expire(0, ids);
        assertThat(a.status()).isEqualTo(QualificationStatus.VALID);
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
         * seq（类型：{@code AtomicLong}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final AtomicLong seq = new AtomicLong(1);

        /**
         * 处理当前类型职责中的操作 {@code nextId}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
         */
        public long nextId() {
            return seq.getAndIncrement();
        }

        /**
         * 处理当前类型职责中的操作 {@code nextBusinessNo}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param prefix 业务处理参数或成员，类型为 {@code String}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String nextBusinessNo(String prefix) {
            return prefix + seq.getAndIncrement();
        }
    }
}
