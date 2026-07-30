package com.chaobo.scm.inventory.domain;

import com.chaobo.scm.common.error.BusinessException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 冻结单聚合状态机和数量边界测试。
 *
 * <p>冻结单先审批、后落账；因此领域测试必须证明审批不能重复、解冻不能超过已经冻结且剩余未解冻的数量。
 */
class StockFreezeAggregateTest {

    @Test
    void approvedFreezeCanBePartiallyAndFullyUnfrozen() {
        StockFreezeAggregate freeze = StockFreezeAggregate.create(
                1L, "FRZ-1", 10L, 88L, 99L, "SKU-1", null,
                new BigDecimal("6"), "QUALITY", "WMS", "QC-1", 100L);

        freeze.submit();
        freeze.approve("APR-1", 200L);
        freeze.unfreeze(new BigDecimal("2"));

        assertThat(freeze.status()).isEqualTo(StockFreezeAggregate.STATUS_PARTIALLY_UNFROZEN);
        assertThat(freeze.remainingFrozenQty()).isEqualByComparingTo("4");

        freeze.unfreeze(new BigDecimal("4"));

        assertThat(freeze.status()).isEqualTo(StockFreezeAggregate.STATUS_UNFROZEN);
        assertThat(freeze.remainingFrozenQty()).isZero();
    }

    @Test
    void approvalAndUnfreezeRespectStateAndQuantityBoundaries() {
        StockFreezeAggregate freeze = StockFreezeAggregate.create(
                1L, "FRZ-1", 10L, 88L, 99L, "SKU-1", null,
                new BigDecimal("3"), "STOCKTAKE", "INVENTORY", "ST-1", 100L);

        assertThatThrownBy(() -> freeze.approve("APR-1", 200L))
                .isInstanceOf(BusinessException.class);

        freeze.submit();
        freeze.approve("APR-1", 200L);

        assertThatThrownBy(() -> freeze.approve("APR-2", 201L))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> freeze.unfreeze(new BigDecimal("4")))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void creatorCannotApproveOwnFreezeRequest() {
        StockFreezeAggregate freeze = StockFreezeAggregate.create(
                1L, "FRZ-1", 10L, 88L, 99L, "SKU-1", null,
                BigDecimal.ONE, "QUALITY", "WMS", "QC-1", 100L);
        freeze.submit();

        assertThatThrownBy(() -> freeze.approve("APR-1", 100L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("申请人与审批人不能相同");
    }
}
