package com.chaobo.scm.inventory.domain;

import com.chaobo.scm.common.error.BusinessException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 库存调整单审批与执行状态机测试。
 *
 * <p>调整会直接改变实物量和可用量，领域层必须阻止未审批执行和同一调整单二次执行。
 */
class InventoryAdjustmentAggregateTest {

    @Test
    void approvedAdjustmentCanExecuteExactlyOnce() {
        InventoryAdjustmentAggregate adjustment = InventoryAdjustmentAggregate.create(
                1L, "ADJ-1", 10L, 88L, 99L, "SKU-1", null,
                new BigDecimal("-2"), "STOCK_LOSS", "盘亏复核", "WMS", "ST-1", 100L);

        adjustment.submit();
        adjustment.approve("APR-1", 200L);
        adjustment.execute(300L);

        assertThat(adjustment.status()).isEqualTo(InventoryAdjustmentAggregate.STATUS_EXECUTED);
        assertThatThrownBy(() -> adjustment.execute(301L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void draftOrRejectedAdjustmentCannotExecute() {
        InventoryAdjustmentAggregate adjustment = InventoryAdjustmentAggregate.create(
                1L, "ADJ-1", 10L, 88L, 99L, "SKU-1", null,
                BigDecimal.ONE, "STOCK_GAIN", "盘盈复核", "WMS", "ST-1", 100L);

        assertThatThrownBy(() -> adjustment.execute(300L))
                .isInstanceOf(BusinessException.class);

        adjustment.submit();
        adjustment.reject("证据不足", 200L);

        assertThat(adjustment.status()).isEqualTo(InventoryAdjustmentAggregate.STATUS_REJECTED);
        assertThatThrownBy(() -> adjustment.execute(300L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void creatorCannotApproveOwnAdjustmentRequest() {
        InventoryAdjustmentAggregate adjustment = InventoryAdjustmentAggregate.create(
                1L, "ADJ-1", 10L, 88L, 99L, "SKU-1", null,
                BigDecimal.ONE, "STOCK_GAIN", "盘盈复核", "WMS", "ST-1", 100L);
        adjustment.submit();

        assertThatThrownBy(() -> adjustment.approve("APR-1", 100L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("申请人与审批人不能相同");
    }
}
