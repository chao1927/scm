package com.chaobo.scm.supplier.domain.profile;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import static org.assertj.core.api.Assertions.*;

/**
 * ProfileChangeAggregateTest。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class ProfileChangeAggregateTest {

    /**
     * ids（类型：{@code Ids}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final Ids ids = new Ids();

    /**
     * 处理当前类型职责中的操作 {@code shouldSubmitAndWithdraw}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shouldSubmitAndWithdraw() {
        var a = ProfileChangeAggregate.submit(10, 2, "更新联系人", List.of(new ProfileFieldChange("contactName", "张三", "李四")), 1, ids);
        assertThat(a.status()).isEqualTo(ProfileChangeStatus.PENDING);
        a.withdraw("资料需调整", 1, ids);
        assertThat(a.status()).isEqualTo(ProfileChangeStatus.WITHDRAWN);
        assertThat(a.pullEvents()).extracting(e -> e.eventType()).containsExactly("SupplierProfileChangeSubmitted", "SupplierProfileChangeWithdrawn");
    }

    /**
     * 处理当前类型职责中的操作 {@code shouldRejectImmutableField}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shouldRejectImmutableField() {
        assertThatThrownBy(() -> new ProfileFieldChange("supplierCode", "S1", "S2")).isInstanceOf(BusinessException.class);
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
