package com.chaobo.scm.supplier.domain.returning;

import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import static org.assertj.core.api.Assertions.*;

/**
 * SupplierReturnAggregateTest。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class SupplierReturnAggregateTest {

    /**
     * ids（类型：{@code TestIdentifierGenerator}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final TestIdentifierGenerator ids = new TestIdentifierGenerator();

    /**
     * 处理当前类型职责中的操作 {@code shouldCompleteReturnOnlyAfterReceiptAndSettlement}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shouldCompleteReturnOnlyAfterReceiptAndSettlement() {
        var a = create();
        long line = a.lines().get(0).id();
        a.submit(1, ids);
        a.review(true, null, 2, ids);
        a.requestInventoryLock(2, ids);
        a.recordInventoryLock(true, "LOCK-1", Map.of(line, bd("10")), null, 0, ids);
        a.supplierConfirm(false, null, 3, ids);
        a.recordOutbound("OUT-1", Map.of(line, bd("10")), 0, ids);
        a.recordWaybill("SHIP-1", "WB-1", "SF", 0, ids);
        a.recordSigned(Map.of(line, bd("10")), null, 0, ids);
        assertThatThrownBy(() -> a.close(2, ids)).hasMessageContaining("结算");
        a.recordSettlement("SET-1", bd("100"), bd("0"), 0, ids);
        a.close(2, ids);
        assertThat(a.status()).isEqualTo(SupplierReturnStatus.CLOSED);
    }

    /**
     * 处理当前类型职责中的操作 {@code shouldRejectOutboundQuantityAboveLockedQuantity}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shouldRejectOutboundQuantityAboveLockedQuantity() {
        var a = create();
        long line = a.lines().get(0).id();
        a.submit(1, ids);
        a.review(true, null, 2, ids);
        a.requestInventoryLock(2, ids);
        a.recordInventoryLock(true, "LOCK-1", Map.of(line, bd("8")), null, 0, ids);
        a.supplierConfirm(false, null, 3, ids);
        assertThatThrownBy(() -> a.recordOutbound("OUT-1", Map.of(line, bd("9")), 0, ids)).hasMessageContaining("锁定数量");
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @return 执行命令的结果，类型为 {@code SupplierReturnAggregate}
     */
    private SupplierReturnAggregate create() {
        return SupplierReturnAggregate.create(1, 2, null, "质量不合格", List.of(new SupplierReturnAggregate.NewLine("SKU-1", "B1", "UNQUALIFIED", bd("10"))), 1, ids);
    }

    /**
     * 处理当前类型职责中的操作 {@code bd}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param v 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
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
        private final AtomicLong n = new AtomicLong();

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
