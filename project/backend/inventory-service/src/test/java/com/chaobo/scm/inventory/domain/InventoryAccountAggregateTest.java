package com.chaobo.scm.inventory.domain;

import com.chaobo.scm.common.error.BusinessException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * InventoryAccountAggregateTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class InventoryAccountAggregateTest {

    /**
     * 执行命令 {@code reserveReleaseFreezeAndOutboundProtectQuantityInvariant}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void reserveReleaseFreezeAndOutboundProtectQuantityInvariant() {
        var account = new InventoryAccountAggregate(1, 1, 1, "SKU", null, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);
        account.receive(BigDecimal.TEN);
        account.reserve(new BigDecimal("4"));
        account.release(new BigDecimal("2"));
        account.freeze(new BigDecimal("3"));
        account.unfreeze(BigDecimal.ONE);
        account.outbound(new BigDecimal("2"));
        assertThat(account.onHandQty()).isEqualByComparingTo("8");
        assertThat(account.availableQty()).isEqualByComparingTo("6");
        assertThat(account.reservedQty()).isZero();
        assertThat(account.frozenQty()).isEqualByComparingTo("2");
    }

    /**
     * 处理当前类型职责中的操作 {@code cannotReserveMoreThanAvailableOrAdjustBelowZero}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void cannotReserveMoreThanAvailableOrAdjustBelowZero() {
        var account = new InventoryAccountAggregate(1, 1, 1, "SKU", null, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);
        assertThatThrownBy(() -> account.reserve(BigDecimal.ONE)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> account.adjust(BigDecimal.ONE.negate())).isInstanceOf(BusinessException.class);
    }
}
