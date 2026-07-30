package com.chaobo.scm.supplier.application.outbox;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * OutboxDispatchApplicationServiceTest。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class OutboxDispatchApplicationServiceTest {

    /**
     * 处理当前类型职责中的操作 {@code shouldMarkPublishedAfterBrokerSuccess}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shouldMarkPublishedAfterBrokerSuccess() {
        var store = new Store();
        var service = new OutboxDispatchApplicationService(store, message -> {
        });
        service.dispatch(message());
        assertThat(store.published).isEqualTo(1);
        assertThat(store.failed).isZero();
    }

    /**
     * 处理当前类型职责中的操作 {@code shouldMarkFailedAfterBrokerFailure}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shouldMarkFailedAfterBrokerFailure() {
        var store = new Store();
        var service = new OutboxDispatchApplicationService(store, message -> {
            throw new IllegalStateException("MQ不可用");
        });
        service.dispatch(message());
        assertThat(store.failed).isEqualTo(1);
        assertThat(store.reason).isEqualTo("MQ不可用");
    }

    /**
     * 处理当前类型职责中的操作 {@code message}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code OutboxMessage}
     */
    private OutboxMessage message() {
        return new OutboxMessage(1, "SUP-1", "SupplierItemEnabled", "SUPPLIER_ITEM", 10, "{}", 0);
    }

    /**
     * Store。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static class Store implements OutboxDispatchPort {

        /**
         * published、failed（类型：{@code int}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        int published, failed;

        /**
         * reason（类型：{@code String}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        String reason;

        /**
         * 处理当前类型职责中的操作 {@code claim}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param b 业务处理参数或成员，类型为 {@code int}
         * @param r 业务处理参数或成员，类型为 {@code int}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code List<OutboxMessage>}
         */
        public List<OutboxMessage> claim(int b, int r) {
            return List.of();
        }

        /**
         * 处理当前类型职责中的操作 {@code markPublished}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param id 业务或技术标识，类型为 {@code long}
         */
        public void markPublished(long id) {
            published++;
        }

        /**
         * 处理当前类型职责中的操作 {@code markFailed}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param reason 业务处理参数或成员，类型为 {@code String}
         */
        public void markFailed(long id, String reason) {
            failed++;
            this.reason = reason;
        }
    }
}
